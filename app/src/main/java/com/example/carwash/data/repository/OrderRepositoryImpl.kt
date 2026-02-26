package com.example.carwash.data.repository

import android.content.ContentResolver
import android.util.Log
import com.example.carwash.data.mapper.toDomain
import com.example.carwash.data.remote.datasource.OrderRemoteDataSource
import com.example.carwash.data.remote.datasource.PhotoRemoteDataSource
import com.example.carwash.data.remote.datasource.StaffRemoteDataSource
import com.example.carwash.data.remote.dto.CreateOrderDto
import com.example.carwash.data.remote.dto.CreateOrderItemDto
import com.example.carwash.data.remote.dto.CreateOrderStaffDto
import com.example.carwash.data.remote.dto.OrderStatus
import com.example.carwash.data.remote.dto.PaymentStatus
import com.example.carwash.data.remote.dto.UpdateOrderStatusDto
import com.example.carwash.domain.model.CreateOrderRequest
import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderStatusHistory
import com.example.carwash.domain.repository.OrderRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OrderRepositoryImpl
@Inject
constructor(
        private val orderDataSource: OrderRemoteDataSource,
        private val staffDataSource: StaffRemoteDataSource,
        private val photoDataSource: PhotoRemoteDataSource,
        private val contentResolver: ContentResolver
) : OrderRepository {

    override suspend fun getOrders(): Result<List<Order>> = runCatching {
        orderDataSource.getAll().map { it.toDomain() }
    }

    override fun getActiveOrders(): Flow<List<Order>> = flow {
        emit(orderDataSource.getActiveOrders().map { it.toDomain() })
    }

    override suspend fun getOrderById(id: String): Result<Order> = runCatching {
        orderDataSource.getByIdWithDetails(id).toDomain()
    }

    override suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> = runCatching {
        val statusStr =
                when (status) {
                    OrderStatus.En_Proceso -> "En Proceso"
                    else -> status.name
                }
        orderDataSource.getByStatus(statusStr).map { it.toDomain() }
    }

    override suspend fun getOrdersByCustomer(customerId: String): Result<List<Order>> =
            runCatching {
                orderDataSource.getByCustomer(customerId).map { it.toDomain() }
            }

    override suspend fun getOrderHistory(orderId: String): Result<List<OrderStatusHistory>> =
            runCatching {
                orderDataSource.getStatusHistory(orderId).map { it.toDomain() }
            }

    override suspend fun addOrder(order: CreateOrderRequest): Result<Order> = runCatching {
        val subtotal = order.items.sumOf { it.subtotal }
        val orderNumber = generateOrderNumber()

        // 1. Upload photos — convert Uri to ByteArray via ContentResolver
        val photoUrls =
                if (order.photos.isNotEmpty()) {
                    val byteArrays =
                            order.photos.mapNotNull { uri ->
                                runCatching {
                                            contentResolver.openInputStream(uri)?.use {
                                                it.readBytes()
                                            }
                                        }
                                        .getOrNull()
                            }
                    if (byteArrays.isNotEmpty()) {
                        try {
                            photoDataSource.uploadPhotos(orderNumber, byteArrays)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error uploading photos, continuing without them", e)
                            emptyList()
                        }
                    } else emptyList()
                } else emptyList()

        // 2. Create the order
        val created =
                orderDataSource.createOrder(
                        CreateOrderDto(
                                orderNumber = orderNumber,
                                customerId = order.customerId,
                                vehicleId = order.vehicleId,
                                cashierId = order.cashierId,
                                subtotal = subtotal,
                                total = subtotal,
                                notes = order.notes,
                                photos = photoUrls,
                                paymentStatus = "pendiente"
                        )
                )

        // 3. Insert order items in batch
        orderDataSource.addOrderItems(
                order.items.map { item ->
                    CreateOrderItemDto(
                            orderId = created.id,
                            serviceId = item.serviceId,
                            serviceName = item.serviceName,
                            unitPrice = item.unitPrice,
                            quantity = item.quantity,
                            subtotal = item.subtotal
                    )
                }
        )

        // 4. Assign staff in batch
        if (order.staffIds.isNotEmpty()) {
            val staffDtos =
                    order.staffIds.mapNotNull { staffId ->
                        runCatching { staffDataSource.getById(staffId) }.getOrNull()?.let { staff ->
                            CreateOrderStaffDto(
                                    orderId = created.id,
                                    staffId = staff.id,
                                    staffName = "${staff.firstName} ${staff.lastName}",
                                    roleSnapshot = staff.role
                            )
                        }
                    }
            if (staffDtos.isNotEmpty()) orderDataSource.assignStaffBatch(staffDtos)
        }

        // 5. Record initial status in history
        orderDataSource.addStatusHistory(
                orderId = created.id,
                status = "Pendiente",
                changedBy = order.cashierId
        )

        // 6. Return with full details
        orderDataSource.getByIdWithDetails(created.id).toDomain()
    }

    override suspend fun updateOrderStatus(
            orderId: String,
            status: OrderStatus,
            changedBy: String?,
            note: String?
    ): Result<Unit> = runCatching {
        val statusStr =
                when (status) {
                    OrderStatus.En_Proceso -> "En Proceso"
                    else -> status.name
                }
        orderDataSource.updateStatus(orderId, UpdateOrderStatusDto(status = statusStr))
        orderDataSource.addStatusHistory(
                orderId = orderId,
                status = statusStr,
                changedBy = changedBy,
                note = note
        )
    }

    override suspend fun cancelOrder(
            orderId: String,
            reason: String,
            changedBy: String?
    ): Result<Unit> = runCatching {
        orderDataSource.updateStatus(
                orderId,
                UpdateOrderStatusDto(status = "Cancelado", cancelReason = reason)
        )
        orderDataSource.addStatusHistory(
                orderId = orderId,
                status = "Cancelado",
                changedBy = changedBy,
                note = reason
        )
    }

    override suspend fun registerPayment(
            orderId: String,
            paymentMethod: String,
            paymentStatus: PaymentStatus
    ): Result<Unit> = runCatching {
        val paymentStr = paymentStatus.name.lowercase()
        orderDataSource.updatePayment(orderId, paymentStr, paymentMethod)
        if (paymentStatus == PaymentStatus.pagado) {
            orderDataSource.updateStatus(
                    orderId,
                    UpdateOrderStatusDto(
                            status = "Entregado",
                            paymentStatus = paymentStr,
                            paymentMethod = paymentMethod
                    )
            )
            orderDataSource.addStatusHistory(
                    orderId = orderId,
                    status = "Entregado",
                    changedBy = null,
                    note = "Pago registrado: $paymentMethod"
            )
        }
    }

    private fun generateOrderNumber(): String =
            "ORD-${System.currentTimeMillis().toString().takeLast(8)}"

    companion object {
        private const val TAG = "OrderRepositoryImpl"
    }
}
