package com.example.carwash.data.repository

import com.example.carwash.data.mapper.toDomain
import com.example.carwash.data.remote.datasource.UserProfileRemoteDataSource
import com.example.carwash.domain.model.UserProfile
import com.example.carwash.domain.repository.UserProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
    private val dataSource: UserProfileRemoteDataSource
) : UserProfileRepository {

    override suspend fun getCurrentUserProfile(): Result<UserProfile?> = runCatching {
        val userId = client.auth.currentSessionOrNull()?.user?.id
            ?: return Result.success(null)
        dataSource.getById(userId)?.toDomain()
    }
}
