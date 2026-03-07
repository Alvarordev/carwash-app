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
import com.example.carwash.data.session.CompanySession
import com.example.carwash.domain.model.CreateOrderRequest
import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderItemRequest
import com.example.carwash.domain.model.OrderPeriod
import com.example.carwash.domain.model.OrderStatusHistory
import com.example.carwash.domain.repository.OrderRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class OrderRepositoryImpl
@Inject
constructor(
        private val orderDataSource: OrderRemoteDataSource,
        private val staffDataSource: StaffRemoteDataSource,
        private val photoDataSource: PhotoRemoteDataSource,
        private val contentResolver: ContentResolver,
        private val companySession: CompanySession
) : OrderRepository {

    override suspend fun getOrders(): Result<List<Order>> = runCatching {
        orderDataSource.getAll().map { it.toDomain() }
    }

    override fun getActiveOrders(): Flow<List<Order>> = flow {
        emit(orderDataSource.getActiveOrders().map { it.toDomain() })
    }

    override fun observeTodayOrders(): Flow<Result<List<Order>>> =
        orderDataSource.observeTodayOrders()
            .map { dtos -> Result.success(dtos.map { it.toDomain() }) }
            .catch { e -> emit(Result.failure(e)) }

    override suspend fun getOrderById(id: String): Result<Order> = runCatching {
        orderDataSource.getByIdWithDetails(id).toDomain()
    }

    override suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> = runCatching {
        val statusStr =
                when (status) {
                    OrderStatus.En_Proceso -> "En Proceso"
                    OrderStatus.Terminado -> "Terminado"
                    OrderStatus.Cancelado -> "Cancelado"
                    OrderStatus.Entregado -> "Entregado"
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
        val companyId = companySession.companyId ?: error("Company session not resolved")
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
                                companyId = companyId,
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
                            companyId = companyId,
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
                                    companyId = companyId,
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
                status = "En Proceso",
                companyId = companyId,
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
        val companyId = companySession.companyId ?: error("Company session not resolved")
        val statusStr =
                when (status) {
                    OrderStatus.En_Proceso -> "En Proceso"
                    OrderStatus.Terminado -> "Terminado"
                    OrderStatus.Cancelado -> "Cancelado"
                    OrderStatus.Entregado -> "Entregado"
                }
        orderDataSource.updateStatus(orderId, UpdateOrderStatusDto(status = statusStr))
        orderDataSource.addStatusHistory(
                orderId = orderId,
                status = statusStr,
                companyId = companyId,
                changedBy = changedBy,
                note = note
        )
    }

    override suspend fun cancelOrder(
            orderId: String,
            reason: String,
            changedBy: String?
    ): Result<Unit> = runCatching {
        val companyId = companySession.companyId ?: error("Company session not resolved")
        orderDataSource.updateStatus(
                orderId,
                UpdateOrderStatusDto(status = "Cancelado", cancelReason = reason)
        )
        orderDataSource.addStatusHistory(
                orderId = orderId,
                status = "Cancelado",
                companyId = companyId,
                changedBy = changedBy,
                note = reason
        )
    }

    override suspend fun registerPayment(
            orderId: String,
            paymentMethod: String,
            paymentStatus: PaymentStatus
    ): Result<Unit> = runCatching {
        val companyId = companySession.companyId ?: error("Company session not resolved")
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
                    companyId = companyId,
                    changedBy = null,
                    note = "Pago registrado: $paymentMethod"
            )
        }
    }

    override suspend fun updateOrderStaff(
        orderId: String,
        toAdd: List<String>,
        toRemove: List<String>
    ): Result<Unit> = runCatching {
        val companyId = companySession.companyId ?: error("Company session not resolved")
        toRemove.forEach { orderStaffId -> orderDataSource.removeStaffFromOrder(orderStaffId) }
        val staffDtos = toAdd.mapNotNull { staffId ->
            runCatching { staffDataSource.getById(staffId) }.getOrNull()?.let { staff ->
                CreateOrderStaffDto(
                    orderId = orderId,
                    companyId = companyId,
                    staffId = staff.id,
                    staffName = "${staff.firstName} ${staff.lastName}",
                    roleSnapshot = staff.role
                )
            }
        }
        if (staffDtos.isNotEmpty()) orderDataSource.assignStaffBatch(staffDtos)
    }

    override suspend fun updateOrderItems(
        orderId: String,
        toAdd: List<OrderItemRequest>,
        toRemove: List<String>
    ): Result<Unit> = runCatching {
        val companyId = companySession.companyId ?: error("Company session not resolved")
        toRemove.forEach { itemId -> orderDataSource.deleteOrderItem(itemId) }
        if (toAdd.isNotEmpty()) {
            orderDataSource.addOrderItems(toAdd.map { item ->
                CreateOrderItemDto(
                    orderId = orderId,
                    companyId = companyId,
                    serviceId = item.serviceId,
                    serviceName = item.serviceName,
                    unitPrice = item.unitPrice,
                    quantity = item.quantity,
                    subtotal = item.subtotal
                )
            })
        }
        val allItems = orderDataSource.getOrderItems(orderId)
        val newSubtotal = allItems.sumOf { it.subtotal }
        orderDataSource.updateTotals(orderId, newSubtotal, 0.0, newSubtotal)
    }

    override fun observeOrdersByPeriod(period: OrderPeriod): Flow<Result<List<Order>>> {
        val zoneId = ZoneId.of("America/Lima")
        val today = LocalDate.now(zoneId)
        val endIso = today.atTime(23, 59, 59).atZone(zoneId).toOffsetDateTime().toString()
        val startIso = when (period) {
            OrderPeriod.Today     -> today.atStartOfDay(zoneId).toOffsetDateTime().toString()
            OrderPeriod.ThisWeek  -> today.with(DayOfWeek.MONDAY).atStartOfDay(zoneId).toOffsetDateTime().toString()
            OrderPeriod.ThisMonth -> today.withDayOfMonth(1).atStartOfDay(zoneId).toOffsetDateTime().toString()
        }
        return orderDataSource.observeOrdersByPeriod(startIso, endIso)
            .map { dtos -> Result.success(dtos.map { it.toDomain() }) }
            .catch { e -> emit(Result.failure(e)) }
    }

    override suspend fun getOrdersByPeriod(period: OrderPeriod): Result<List<Order>> = runCatching {
        val zoneId = ZoneId.of("America/Lima")
        val today = LocalDate.now(zoneId)
        val endIso = today.atTime(23, 59, 59).atZone(zoneId).toOffsetDateTime().toString()
        val startIso = when (period) {
            OrderPeriod.Today     -> today.atStartOfDay(zoneId).toOffsetDateTime().toString()
            OrderPeriod.ThisWeek  -> today.with(DayOfWeek.MONDAY).atStartOfDay(zoneId).toOffsetDateTime().toString()
            OrderPeriod.ThisMonth -> today.withDayOfMonth(1).atStartOfDay(zoneId).toOffsetDateTime().toString()
        }
        orderDataSource.getAllForPeriod(startIso, endIso).map { it.toDomain() }
    }

    private fun generateOrderNumber(): String =
            "ORD-${System.currentTimeMillis().toString().takeLast(8)}"

    companion object {
        private const val TAG = "OrderRepositoryImpl"
    }
}
