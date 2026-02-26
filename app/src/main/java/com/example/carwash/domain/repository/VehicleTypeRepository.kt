package com.example.carwash.domain.repository

import com.example.carwash.domain.model.VehicleType
import kotlinx.coroutines.flow.Flow

interface VehicleTypeRepository {
    fun getVehicleTypes(): Flow<List<VehicleType>>
    suspend fun getActiveVehicleTypes(): Result<List<VehicleType>>
    suspend fun addVehicleType(vehicleType: VehicleType): Result<VehicleType>
    suspend fun updateVehicleType(vehicleType: VehicleType): Result<Unit>
}