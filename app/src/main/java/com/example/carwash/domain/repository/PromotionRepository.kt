package com.example.carwash.domain.repository

import com.example.carwash.domain.model.Promotion
import kotlinx.coroutines.flow.Flow

interface PromotionRepository {
    fun getPromotions(): Flow<List<Promotion>>
    suspend fun getActivePromotions(): Result<List<Promotion>>
    suspend fun addPromotion(promotion: Promotion): Result<Promotion>
    suspend fun updatePromotion(promotion: Promotion): Result<Unit>
}