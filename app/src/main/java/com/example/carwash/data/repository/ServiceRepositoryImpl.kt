package com.example.carwash.data.repository

import com.example.carwash.data.mapper.toDomain
import com.example.carwash.data.remote.datasource.ServicePricingRemoteDataSource
import com.example.carwash.data.remote.datasource.ServiceRemoteDataSource
import com.example.carwash.data.remote.dto.ServiceDto
import com.example.carwash.domain.model.EntityStatus
import com.example.carwash.domain.model.Service
import com.example.carwash.domain.model.ServiceCategory
import com.example.carwash.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ServiceRepositoryImpl @Inject constructor(
    private val serviceDataSource: ServiceRemoteDataSource,
    private val pricingDataSource: ServicePricingRemoteDataSource
) : ServiceRepository {

    override fun getServices(): Flow<List<Service>> = flow {
        emit(serviceDataSource.getAll().map { it.toDomain() })
    }

    override suspend fun getActiveServices(): Result<List<Service>> = runCatching {
        serviceDataSource.getActive().map { it.toDomain() }
    }

    override suspend fun getServicesByCategory(category: ServiceCategory): Result<List<Service>> = runCatching {
        val categoryStr = when (category) {
            ServiceCategory.Aniadido -> "añadido"
            else -> category.name.lowercase()
        }
        serviceDataSource.getByCategory(categoryStr).map { it.toDomain() }
    }

    override suspend fun getServicePricing(
        serviceId: String,
        vehicleTypeId: String
    ) = runCatching {
        pricingDataSource.getPrice(serviceId, vehicleTypeId)?.toDomain()
    }

    override suspend fun getAllPricing() = runCatching {
        pricingDataSource.getAll().map { it.toDomain() }
    }

    override suspend fun addService(service: Service): Result<Service> = runCatching {
        serviceDataSource.insert(service.toDto()).toDomain()
    }

    override suspend fun updateService(service: Service): Result<Unit> = runCatching {
        serviceDataSource.update(service.id, service.toDto())
    }

    private fun Service.toDto() = ServiceDto(
        id = id,
        name = name,
        description = description,
        category = when (category) {
            ServiceCategory.Aniadido -> "añadido"
            else -> category.name.lowercase()
        },
        status = if (status == EntityStatus.Active) "active" else "inactive",
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}