package com.example.carwash.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carwash.data.local.ThemePreferenceManager
import com.example.carwash.domain.model.ThemePreference
import com.example.carwash.domain.repository.AuthRepository
import com.example.carwash.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userFullName: String = "",
    val userRole: String = "",
    val userEmail: String = "",
    val isLoadingProfile: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.System
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val themePreferenceManager: ThemePreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeTheme()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userProfileRepository.getCurrentUserProfile()
                .onSuccess { profile ->
                    if (profile != null) {
                        _uiState.update {
                            it.copy(
                                userFullName = profile.fullName,
                                userRole = profile.role.displayName,
                                userEmail = profile.email.orEmpty(),
                                isLoadingProfile = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoadingProfile = false) }
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to load user profile", error)
                    _uiState.update { it.copy(isLoadingProfile = false) }
                }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themePreferenceManager.themeFlow.collect { pref ->
                _uiState.update { it.copy(themePreference = pref) }
            }
        }
    }

    fun setTheme(preference: ThemePreference) {
        viewModelScope.launch {
            themePreferenceManager.setTheme(preference)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { authRepository.signOut() }
                .onFailure { Log.e(TAG, "Sign out failed", it) }
        }
    }

    private companion object {
        const val TAG = "SettingsViewModel"
    }
}
