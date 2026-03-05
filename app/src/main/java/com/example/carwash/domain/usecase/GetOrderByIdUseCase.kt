package com.example.carwash.domain.usecase

import com.example.carwash.domain.model.Order
import com.example.carwash.domain.repository.OrderRepository
import javax.inject.Inject

class GetOrderByIdUseCase @Inject constructor(private val repository: OrderRepository) {
    suspend operator fun invoke(id: String): Result<Order> = repository.getOrderById(id)
}
