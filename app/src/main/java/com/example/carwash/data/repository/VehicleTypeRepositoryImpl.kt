package com.example.carwash.data.repository

import com.example.carwash.data.mapper.toDomain
import com.example.carwash.data.remote.datasource.VehicleTypeRemoteDataSource
import com.example.carwash.data.remote.dto.VehicleTypeDto
import com.example.carwash.domain.model.EntityStatus
import com.example.carwash.domain.model.VehicleType
import com.example.carwash.domain.repository.VehicleTypeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class VehicleTypeRepositoryImpl @Inject constructor(
    private val dataSource: VehicleTypeRemoteDataSource
) : VehicleTypeRepository {

    override fun getVehicleTypes(): Flow<List<VehicleType>> = flow {
        emit(dataSource.getAll().map { it.toDomain() })
    }

    override suspend fun getActiveVehicleTypes(): Result<List<VehicleType>> = runCatching {
        dataSource.getActive().map { it.toDomain() }
    }

    override suspend fun addVehicleType(vehicleType: VehicleType): Result<VehicleType> = runCatching {
        dataSource.insert(vehicleType.toDto()).toDomain()
    }

    override suspend fun updateVehicleType(vehicleType: VehicleType): Result<Unit> = runCatching {
        dataSource.update(vehicleType.id, vehicleType.toDto())
    }

    private fun VehicleType.toDto() = VehicleTypeDto(
        id = id,
        name = name,
        description = description,
        status = if (status == EntityStatus.Active) "active" else "inactive",
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}