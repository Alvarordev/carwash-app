package com.example.carwash.data.repository

import com.example.carwash.data.mapper.toDomain
import com.example.carwash.data.remote.datasource.PromotionRemoteDataSource
import com.example.carwash.data.remote.dto.PromotionDto
import com.example.carwash.domain.model.EntityStatus
import com.example.carwash.domain.model.Promotion
import com.example.carwash.domain.repository.PromotionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PromotionRepositoryImpl @Inject constructor(
    private val dataSource: PromotionRemoteDataSource
) : PromotionRepository {

    override fun getPromotions(): Flow<List<Promotion>> = flow {
        emit(dataSource.getAll().map { it.toDomain() })
    }

    override suspend fun getActivePromotions(): Result<List<Promotion>> = runCatching {
        dataSource.getActive().map { it.toDomain() }
    }

    override suspend fun addPromotion(promotion: Promotion): Result<Promotion> = runCatching {
        dataSource.insert(promotion.toDto()).toDomain()
    }

    override suspend fun updatePromotion(promotion: Promotion): Result<Unit> = runCatching {
        dataSource.update(promotion.id, promotion.toDto())
    }

    private fun Promotion.toDto() = PromotionDto(
        id = id,
        name = name,
        description = description,
        discountType = discountType.name.lowercase(),
        discountValue = discountValue,
        scope = when (scope.name) {
            "VehicleType" -> "vehicleType"
            else -> scope.name.lowercase()
        },
        startDate = startDate?.toString(),
        endDate = endDate?.toString(),
        status = if (status == EntityStatus.Active) "active" else "inactive",
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}
