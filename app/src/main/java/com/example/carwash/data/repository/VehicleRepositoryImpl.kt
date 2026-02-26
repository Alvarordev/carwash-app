package com.example.carwash.data.repository

import com.example.carwash.data.mapper.toDomain
import com.example.carwash.data.remote.datasource.VehicleRemoteDataSource
import com.example.carwash.data.remote.dto.VehicleDto
import com.example.carwash.domain.model.EntityStatus
import com.example.carwash.domain.model.Vehicle
import com.example.carwash.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val dataSource: VehicleRemoteDataSource
) : VehicleRepository {

    override fun getVehicles(): Flow<List<Vehicle>> = flow {
        emit(dataSource.getAll().map { it.toDomain() })
    }

    override suspend fun getVehicleById(id: String): Result<Vehicle> = runCatching {
        dataSource.getById(id).toDomain()
    }

    override suspend fun getVehicleByPlate(plate: String): Result<Vehicle?> = runCatching {
        dataSource.getByPlate(plate)?.toDomain()
    }

    override suspend fun getVehiclesByCustomer(customerId: String): Result<List<Vehicle>> = runCatching {
        dataSource.getByCustomerId(customerId).map { it.toDomain() }
    }

    override suspend fun addVehicle(vehicle: Vehicle, customerId: String?): Result<Vehicle> = runCatching {
        val created = dataSource.insert(vehicle.toDto()).toDomain()
        customerId?.let { dataSource.linkOwner(created.id, it) }
        created
    }

    override suspend fun updateVehicle(vehicle: Vehicle): Result<Unit> = runCatching {
        dataSource.update(vehicle.id, vehicle.toDto())
    }

    private fun Vehicle.toDto() = VehicleDto(
        id = id,
        plate = plate,
        color = color,
        brand = brand,
        model = model,
        vehicleTypeId = vehicleTypeId,
        status = if (status == EntityStatus.Active) "active" else "inactive",
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}