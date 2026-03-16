package com.example.carwash.data.repository

import android.util.Log
import com.example.carwash.data.remote.datasource.CompanyRemoteDataSource
import com.example.carwash.data.session.CompanySession
import com.example.carwash.domain.model.AppSessionState
import com.example.carwash.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
    private val companyDataSource: CompanyRemoteDataSource,
    private val companySession: CompanySession
) : AuthRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val restoreMutex = Mutex()
    private val _appSessionState = MutableStateFlow<AppSessionState>(AppSessionState.Restoring)

    init {
        scope.launch {
            client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> reconcileSession(forceBlocking = !companySession.isResolved)
                    is SessionStatus.Initializing -> {
                        if (_appSessionState.value !is AppSessionState.Authenticated) {
                            _appSessionState.value = AppSessionState.Restoring
                        }
                    }

                    else -> {
                        companySession.clear()
                        _appSessionState.value = AppSessionState.Unauthenticated
                    }
                }
            }
        }
    }

    override val sessionStatus: StateFlow<SessionStatus> = client.auth.sessionStatus
    override val appSessionState: StateFlow<AppSessionState> = _appSessionState.asStateFlow()

    override suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        reconcileSession(forceBlocking = true)
    }

    override suspend fun signOut() {
        client.auth.signOut()
        companySession.clear()
        _appSessionState.value = AppSessionState.Unauthenticated
    }

    private suspend fun reconcileSession(forceBlocking: Boolean) {
        restoreMutex.withLock {
            val session = client.auth.currentSessionOrNull()
            val user = session?.user

            if (user == null) {
                companySession.clear()
                _appSessionState.value = AppSessionState.Unauthenticated
                return
            }

            val tokenCompanyId = user.appMetadata
                ?.get("company_id")
                ?.jsonPrimitive
                ?.contentOrNull
            val tokenStaffName = user.userMetadata
                ?.get("first_name")
                ?.jsonPrimitive
                ?.contentOrNull
                ?.takeIf { it.isNotBlank() }

            companySession.bootstrap(companyId = tokenCompanyId, staffName = tokenStaffName)

            val optimisticCompanyId = companySession.companyId
            if (optimisticCompanyId != null) {
                _appSessionState.value = AppSessionState.Authenticated(
                    companyId = optimisticCompanyId,
                    staffName = companySession.staffName,
                    staffMemberId = companySession.staffMemberId,
                    isReconciling = companySession.staffMemberId == null
                )
            } else {
                _appSessionState.value = AppSessionState.Restoring
            }

            val email = user.email
            if (email.isNullOrBlank()) {
                if (companySession.companyId == null) {
                    companySession.clear()
                    _appSessionState.value = AppSessionState.Unauthenticated
                }
                return
            }

            val needsRemoteReconciliation = forceBlocking ||
                companySession.staffMemberId == null ||
                companySession.companyId == null

            if (!needsRemoteReconciliation) {
                return
            }

            companySession.setReconciling(true)

            val reconciliationResult = runCatching {
                companyDataSource.getStaffByEmail(email)
            }

            companySession.setReconciling(false)

            reconciliationResult
                .onSuccess { staff ->
                    if (staff != null) {
                        companySession.bootstrap(
                            companyId = staff.companyId,
                            staffName = companySession.staffName ?: staff.firstName.takeIf { it.isNotBlank() }
                        )
                        companySession.setStaffMember(staff.id)
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to reconcile company session", error)
                }

            val resolvedCompanyId = companySession.companyId
            if (resolvedCompanyId != null) {
                _appSessionState.value = AppSessionState.Authenticated(
                    companyId = resolvedCompanyId,
                    staffName = companySession.staffName,
                    staffMemberId = companySession.staffMemberId,
                    isReconciling = companySession.isReconciling || companySession.staffMemberId == null
                )
            } else if (forceBlocking) {
                companySession.clear()
                _appSessionState.value = AppSessionState.Unauthenticated
            }
        }
    }

    private companion object {
        const val TAG = "AuthRepository"
    }
}
