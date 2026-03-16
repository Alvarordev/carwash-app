package com.example.carwash.domain.repository

import com.example.carwash.domain.model.UserProfile

interface UserProfileRepository {
    suspend fun getCurrentUserProfile(): Result<UserProfile?>
}
