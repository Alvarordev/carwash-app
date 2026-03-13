package com.example.carwash.data.repository

import com.example.carwash.data.remote.datasource.CompanyRemoteDataSource
import com.example.carwash.data.session.CompanySession
import com.example.carwash.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
    private val companyDataSource: CompanyRemoteDataSource,
    private val companySession: CompanySession
) : AuthRepository {

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            client.auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated && !companySession.isResolved) {
                    try {
                        resolveCompanySession()
                    } catch (e: Exception) {
                        Log.e("AuthRepository", "Failed to resolve company session (no internet?)", e)
                    }
                }
            }
        }
    }

    override val sessionStatus: StateFlow<SessionStatus> = client.auth.sessionStatus

    override suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        resolveCompanySession()
    }

    override suspend fun signOut() {
        client.auth.signOut()
        companySession.clear()
    }

    private suspend fun resolveCompanySession() {
        val user = client.auth.currentSessionOrNull()?.user ?: return
        val companyId = user.appMetadata?.get("company_id")?.jsonPrimitive?.content ?: return
        companySession.companyId = companyId

        companySession.staffName = user.userMetadata
            ?.get("first_name")?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }

        val email = user.email ?: return
        val staff = companyDataSource.getStaffByEmail(email)
        companySession.staffMemberId = staff?.id
    }
}
