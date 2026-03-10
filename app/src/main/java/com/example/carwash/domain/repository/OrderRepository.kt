package com.example.carwash.domain.repository

import android.net.Uri
import com.example.carwash.data.remote.dto.OrderStatus
import com.example.carwash.data.remote.dto.PaymentStatus
import com.example.carwash.domain.model.CreateOrderRequest
import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderItemRequest
import com.example.carwash.domain.model.OrderPeriod
import com.example.carwash.domain.model.OrderStatusHistory
import com.example.carwash.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun getOrders():  Result<List<Order>>
    fun getActiveOrders(): Flow<List<Order>>
    fun observeTodayOrders(): Flow<Result<List<Order>>>
    suspend fun getOrderById(id: String): Result<Order>
    suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>>
    suspend fun getOrdersByCustomer(customerId: String): Result<List<Order>>
    suspend fun getOrderHistory(orderId: String): Result<List<OrderStatusHistory>>
    suspend fun addOrder(order: CreateOrderRequest): Result<Order>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus, changedBy: String? = null, note: String? = null): Result<Unit>
    suspend fun cancelOrder(orderId: String, reason: String, changedBy: String? = null): Result<Unit>
    suspend fun registerPayment(orderId: String, paymentMethod: String, paymentStatus: PaymentStatus): Result<Unit>
    suspend fun getOrdersByPeriod(period: OrderPeriod): Result<List<Order>>
    fun observeOrdersByPeriod(period: OrderPeriod): Flow<Result<List<Order>>>
    suspend fun updateOrderStaff(
        orderId: String,
        toAdd: List<String>,
        toRemove: List<String>
    ): Result<Unit>

    suspend fun updateOrderItems(
        orderId: String,
        toAdd: List<OrderItemRequest>,
        toRemove: List<String>
    ): Result<Unit>

    suspend fun getPaymentMethods(): Result<List<PaymentMethod>>
    suspend fun deliverOrder(orderId: String, paymentMethod: String, newPhotoUris: List<Uri>): Result<Unit>
}