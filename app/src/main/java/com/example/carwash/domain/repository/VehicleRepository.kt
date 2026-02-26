package com.example.carwash.domain.repository

import com.example.carwash.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun getVehicles(): Flow<List<Vehicle>>
    suspend fun getVehicleById(id: String): Result<Vehicle>
    suspend fun getVehicleByPlate(plate: String): Result<Vehicle?>
    suspend fun getVehiclesByCustomer(customerId: String): Result<List<Vehicle>>
    suspend fun addVehicle(vehicle: Vehicle, customerId: String? = null): Result<Vehicle>
    suspend fun updateVehicle(vehicle: Vehicle): Result<Unit>
}