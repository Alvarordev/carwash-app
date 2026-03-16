package com.example.carwash.domain.repository

import com.example.carwash.domain.model.Service
import com.example.carwash.domain.model.ServicePricing
import kotlinx.coroutines.flow.Flow

interface ServiceRepository {
    fun getServices(): Flow<List<Service>>
    suspend fun getActiveServices(): Result<List<Service>>
    suspend fun getServicesByCategoryId(categoryId: String): Result<List<Service>>
    suspend fun getServicePricing(serviceId: String, vehicleTypeId: String): Result<ServicePricing?>
    suspend fun getAllPricing(): Result<List<ServicePricing>>
    suspend fun getPricingByVehicleType(vehicleTypeId: String): Result<List<ServicePricing>>
    suspend fun addService(service: Service): Result<Service>
    suspend fun updateService(service: Service): Result<Unit>
}