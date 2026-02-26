package com.example.carwash.domain.repository

import com.example.carwash.data.remote.dto.OrderStatus
import com.example.carwash.data.remote.dto.PaymentStatus
import com.example.carwash.domain.model.CreateOrderRequest
import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderStatusHistory
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun getOrders():  Result<List<Order>>
    fun getActiveOrders(): Flow<List<Order>>
    suspend fun getOrderById(id: String): Result<Order>
    suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>>
    suspend fun getOrdersByCustomer(customerId: String): Result<List<Order>>
    suspend fun getOrderHistory(orderId: String): Result<List<OrderStatusHistory>>
    suspend fun addOrder(order: CreateOrderRequest): Result<Order>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus, changedBy: String? = null, note: String? = null): Result<Unit>
    suspend fun cancelOrder(orderId: String, reason: String, changedBy: String? = null): Result<Unit>
    suspend fun registerPayment(orderId: String, paymentMethod: String, paymentStatus: PaymentStatus): Result<Unit>
}