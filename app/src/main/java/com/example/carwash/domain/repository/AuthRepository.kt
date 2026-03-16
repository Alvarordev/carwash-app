package com.example.carwash.domain.repository

import com.example.carwash.domain.model.AppSessionState
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionStatus: StateFlow<SessionStatus>
    val appSessionState: StateFlow<AppSessionState>
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
}
