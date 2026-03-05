package com.example.carwash.domain.usecase

import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderPeriod
import com.example.carwash.domain.repository.OrderRepository
import javax.inject.Inject

class GetOrdersByPeriodUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(period: OrderPeriod): Result<List<Order>> =
        repository.getOrdersByPeriod(period)
}
