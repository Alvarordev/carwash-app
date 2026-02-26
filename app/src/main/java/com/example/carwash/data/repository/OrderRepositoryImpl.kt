package com.example.carwash.data.repository

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val orderDataSource: OrderRemoteDataSource,
    private val staffDataSource: StaffRemoteDataSource,
    private val photoDataSource: PhotoRemoteDataSource
) : OrderRepository {

    override suspend fun getOrders():  Result<List<Order>> = runCatching {
        orderDataSource.getAll().map { it.toDomain() }
    }

    override fun getActiveOrders(): Flow<List<Order>> = flow {
        emit(orderDataSource.getActiveOrders().map { it.toDomain() })
    }

    override suspend fun getOrderById(id: String): Result<Order> = runCatching {
        orderDataSource.getByIdWithDetails(id).toDomain()
    }

    override suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> = runCatching {
        val statusStr = when (status) {
            OrderStatus.En_Proceso -> "En Proceso"
            else -> status.name
        }
        orderDataSource.getByStatus(statusStr).map { it.toDomain() }
    }

    override suspend fun getOrdersByCustomer(customerId: String): Result<List<Order>> = runCatching {
        orderDataSource.getByCustomer(customerId).map { it.toDomain() }
    }

    override suspend fun getOrderHistory(orderId: String): Result<List<OrderStatusHistory>> = runCatching {
        orderDataSource.getStatusHistory(orderId).map { it.toDomain() }
    }

    override suspend fun addOrder(order: CreateOrderRequest): Result<Order> = runCatching {
        val subtotal = order.items.sumOf { it.subtotal }
        val orderNumber = generateOrderNumber()

        val photoUrls = if (order.photos.isNotEmpty()) {
            photoDataSource.uploadPhotos(orderNumber, order.photos)
        } else emptyList()

        // 2. Crear la orden con las URLs ya incluidas
        val created = orderDataSource.createOrder(
            CreateOrderDto(
                orderNumber = orderNumber,
                customerId = order.customerId,
                vehicleId = order.vehicleId,
                cashierId = order.cashierId,
                subtotal = subtotal,
                total = subtotal,
                notes = order.notes,
                photos = photoUrls   // ← URLs del storage
            )
        )

        // 3. Insertar items en batch
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

        // 4. Asignar staff en batch (con snapshot del nombre y rol)
        if (order.staffIds.isNotEmpty()) {
            val staffDtos = order.staffIds.mapNotNull { staffId ->
                runCatching { staffDataSource.getById(staffId) }.getOrNull()
                    ?.let { staff ->
                        CreateOrderStaffDto(
                            orderId = created.id,
                            staffId = staff.id,
                            staffName = "${staff.firstName} ${staff.lastName}",
                            roleSnapshot = staff.role
                        )
                    }
            }
            orderDataSource.assignStaffBatch(staffDtos)
        }

        // 5. Registrar estado inicial en historial
        orderDataSource.addStatusHistory(
            orderId = created.id,
            status = "Pendiente",
            changedBy = order.cashierId
        )

        // 6. Retornar con detalles completos
        orderDataSource.getByIdWithDetails(created.id).toDomain()
    }

    override suspend fun updateOrderStatus(
        orderId: String,
        status: OrderStatus,
        changedBy: String?,
        note: String?
    ): Result<Unit> = runCatching {
        val statusStr = when (status) {
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
}