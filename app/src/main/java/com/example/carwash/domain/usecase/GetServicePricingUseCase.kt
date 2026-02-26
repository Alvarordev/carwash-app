package com.example.carwash.domain.usecase

import com.example.carwash.domain.model.ServicePricing
import com.example.carwash.domain.repository.ServiceRepository
import javax.inject.Inject

class GetServicePricingUseCase @Inject constructor(private val repository: ServiceRepository) {
    suspend operator fun invoke(serviceId: String, vehicleTypeId: String): ServicePricing? =
            repository.getServicePricing(serviceId, vehicleTypeId).getOrNull()
}
