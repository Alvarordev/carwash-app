package com.example.carwash.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the resolved company context after a successful login.
 * Populated by [AuthRepositoryImpl] after sign-in and cleared on sign-out.
 */
@Singleton
class CompanySession @Inject constructor() {
    data class Snapshot(
        val companyId: String? = null,
        val staffMemberId: String? = null,
        val staffName: String? = null
    )

    private val _snapshot = MutableStateFlow(Snapshot())
    val snapshot: StateFlow<Snapshot> = _snapshot.asStateFlow()

    val companyId: String?
        get() = _snapshot.value.companyId

    val staffMemberId: String?
        get() = _snapshot.value.staffMemberId

    val staffName: String?
        get() = _snapshot.value.staffName

    fun bootstrap(companyId: String?, staffName: String?) {
        _snapshot.value = _snapshot.value.copy(
            companyId = companyId ?: _snapshot.value.companyId,
            staffName = staffName ?: _snapshot.value.staffName
        )
    }

    fun setStaffMember(staffMemberId: String?) {
        _snapshot.value = _snapshot.value.copy(staffMemberId = staffMemberId)
    }

    fun clear() {
        _snapshot.value = Snapshot()
    }

    val isResolved: Boolean get() = companyId != null
}
