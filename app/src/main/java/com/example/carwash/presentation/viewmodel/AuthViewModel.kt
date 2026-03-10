package com.example.carwash.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carwash.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val sessionStatus: StateFlow<SessionStatus> = authRepository.sessionStatus
        .stateIn(
            scope = viewModelScope,
            initialValue = SessionStatus.Initializing,
            started = SharingStarted.WhileSubscribed(5000)
        )

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                authRepository.signIn(email, password)
            } catch (e: Exception) {
                _error.value = mapAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun mapAuthError(e: Exception): String {
        val msg = e.message.orEmpty().lowercase()
        return when {
            "invalid login credentials" in msg || "invalid_grant" in msg ->
                "Correo o contraseña incorrectos"
            "email not confirmed" in msg ->
                "Tu correo aún no ha sido confirmado"
            "user not found" in msg ->
                "No se encontró una cuenta con ese correo"
            "too many requests" in msg || "rate limit" in msg ->
                "Demasiados intentos. Espera un momento e intenta de nuevo"
            "network" in msg || "unable to resolve host" in msg || "timeout" in msg ->
                "Sin conexión a internet. Verifica tu red"
            else -> "Error al iniciar sesión. Intenta de nuevo"
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al cerrar sesión"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
