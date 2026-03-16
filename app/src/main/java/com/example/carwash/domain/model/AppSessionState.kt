package com.example.carwash.domain.model

sealed class AppSessionState {
    object Restoring : AppSessionState()
    object Unauthenticated : AppSessionState()

    data class Authenticated(
        val companyId: String,
        val staffName: String? = null,
        val staffMemberId: String? = null,
        val isReconciling: Boolean = false
    ) : AppSessionState()
}
