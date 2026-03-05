package com.example.carwash.data.session

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the resolved company context after a successful login.
 * Populated by [AuthRepositoryImpl] after sign-in and cleared on sign-out.
 */
@Singleton
class CompanySession @Inject constructor() {
    var companyId: String? = null
    var staffMemberId: String? = null
    var staffName: String? = null

    fun clear() {
        companyId = null
        staffMemberId = null
        staffName = null
    }

    val isResolved: Boolean get() = companyId != null
}
