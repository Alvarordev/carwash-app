package com.example.carwash.domain.usecase

import com.example.carwash.domain.model.VehicleType
import com.example.carwash.domain.repository.VehicleTypeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVehicleTypesUseCase @Inject constructor(
    private val repository: VehicleTypeRepository
) {
    operator fun invoke(): Flow<List<VehicleType>> {
        return repository.getVehicleTypes()
    }
}