package com.example.carwash.domain.repository

import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val sessionStatus: StateFlow<SessionStatus>
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
}
