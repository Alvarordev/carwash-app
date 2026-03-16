package com.example.carwash.domain.usecase

import com.example.carwash.domain.model.Order
import com.example.carwash.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOrderByIdUseCase @Inject constructor(private val repository: OrderRepository) {
    fun observe(id: String): Flow<Order?> = repository.observeCachedOrderById(id)

    suspend operator fun invoke(id: String): Result<Order> = repository.getOrderById(id)

    suspend fun refresh(id: String): Result<Order> = repository.refreshOrderById(id)
}
