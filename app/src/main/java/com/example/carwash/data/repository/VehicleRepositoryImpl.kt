package com.example.carwash.data.repository

import com.example.carwash.data.mapper.toDomain
import com.example.carwash.data.remote.datasource.VehicleRemoteDataSource
import com.example.carwash.data.remote.dto.VehicleDto
import com.example.carwash.data.session.CompanySession
import com.example.carwash.domain.model.EntityStatus
import com.example.carwash.domain.model.Vehicle
import com.example.carwash.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val dataSource: VehicleRemoteDataSource,
    private val companySession: CompanySession
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
        val companyId = companySession.companyId ?: error("Company session not resolved")
        val created = dataSource.insert(vehicle.toDto(companyId)).toDomain()
        customerId?.let { dataSource.linkOwner(created.id, it, companyId) }
        created
    }

    override suspend fun updateVehicle(vehicle: Vehicle): Result<Unit> = runCatching {
        dataSource.update(vehicle.id, vehicle.toDto(companySession.companyId))
    }

    override suspend fun linkOwnerToVehicle(vehicleId: String, customerId: String): Result<Unit> = runCatching {
        val companyId = companySession.companyId ?: error("Company session not resolved")
        try {
            dataSource.linkOwner(vehicleId, customerId, companyId)
        } catch (e: Exception) {
            // Ignore duplicate owner links (composite PK conflict)
            if (e.message?.contains("duplicate") == true || e.message?.contains("23505") == true) Unit
            else throw e
        }
    }

    private fun Vehicle.toDto(companyId: String? = null) = VehicleDto(
        id = id.ifBlank { null },    // null → DB generates UUID on insert
        plate = plate,
        color = color,
        brand = brand,
        model = model,
        vehicleTypeId = vehicleTypeId,
        status = if (status == EntityStatus.Active) "active" else "inactive",
        createdAt = if (createdAt != null) createdAt.toString() else null,
        updatedAt = if (updatedAt != null) updatedAt.toString() else null,
        companyId = companyId
    )
}
