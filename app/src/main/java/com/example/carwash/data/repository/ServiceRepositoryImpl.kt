package com.example.carwash.data.repository

import com.example.carwash.data.mapper.toDomain
import com.example.carwash.data.remote.datasource.ServicePricingRemoteDataSource
import com.example.carwash.data.remote.datasource.ServiceRemoteDataSource
import com.example.carwash.domain.model.Service
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

    override suspend fun getActiveServices() = runCatching {
        serviceDataSource.getActive().map { it.toDomain() }
    }

    override suspend fun getServicesByCategoryId(categoryId: String) = runCatching {
        serviceDataSource.getByCategoryId(categoryId).map { it.toDomain() }
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

    override suspend fun getPricingByVehicleType(vehicleTypeId: String) = runCatching {
        pricingDataSource.getByVehicleType(vehicleTypeId).map { it.toDomain() }
    }

    override suspend fun addService(service: Service) = runCatching {
        serviceDataSource.insert(service.toDto()).toDomain()
    }

    override suspend fun updateService(service: Service) = runCatching {
        serviceDataSource.update(service.id, service.toDto())
    }

    private fun Service.toDto() = com.example.carwash.data.remote.dto.ServiceDto(
        id = id,
        name = name,
        description = description,
        categoryId = categoryId,
        status = if (status == com.example.carwash.domain.model.EntityStatus.Active) "active" else "inactive",
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        color = color,
        icon = icon
    )
}
