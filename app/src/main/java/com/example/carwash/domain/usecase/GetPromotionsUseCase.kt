package com.example.carwash.domain.usecase

import com.example.carwash.domain.model.Promotion
import com.example.carwash.domain.repository.PromotionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPromotionsUseCase @Inject constructor(
    private val repository: PromotionRepository
) {
    operator fun invoke(): Flow<List<Promotion>> {
        return repository.getPromotions()
    }
}