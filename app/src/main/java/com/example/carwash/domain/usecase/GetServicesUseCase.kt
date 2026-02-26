package com.example.carwash.domain.usecase

import com.example.carwash.domain.model.Service
import com.example.carwash.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetServicesUseCase @Inject constructor(
    private val repository: ServiceRepository
) {
    operator fun invoke(): Flow<List<Service>> {
        return repository.getServices()
    }
}