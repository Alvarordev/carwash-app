package com.example.carwash.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.carwash.data.local.dao.OrderDao
import com.example.carwash.data.mapper.toCachedOrderGraph
import com.example.carwash.data.mapper.toDomain
import com.example.carwash.data.remote.datasource.OrderRemoteDataSource
import com.example.carwash.data.remote.datasource.PaymentMethodRemoteDataSource
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
import com.example.carwash.domain.model.PaymentMethod
import com.example.carwash.domain.repository.OrderRepository
import com.example.carwash.util.ImageCompressor
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OrderRepositoryImpl @Inject constructor(
    private val orderDataSource: OrderRemoteDataSource,
    private val staffDataSource: StaffRemoteDataSource,
    private val photoDataSource: PhotoRemoteDataSource,
    private val paymentMethodDataSource: PaymentMethodRemoteDataSource,
    private val orderDao: OrderDao,
    private val contentResolver: ContentResolver,
    private val companySession: CompanySession
) : OrderRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val zoneId = ZoneId.of("America/Lima")

    private fun requireCompanyId(): String =
        companySession.companyId ?: throw IllegalStateException("La sesión aún se está sincronizando")

    override suspend fun getOrders(): Result<List<Order>> = runCatching {
        orderDataSource.getAll().map { it.toDomain() }
    }

    override fun getActiveOrders(): Flow<List<Order>> = flow {
        val companyId = requireCompanyId()
        val endDate = LocalDate.now(zoneId)
        val startDate = endDate.minusDays(30)
        repositoryScope.launch {
            refreshOrdersRange(companyId, startDate, endDate)
        }
        emitAll(
            orderDao.observeOrdersByRange(
                companyId = companyId,
                startIso = startOfDayIso(startDate),
                endIso = endOfDayIso(endDate)
            ).map { orders ->
                orders.map { it.toDomain() }.filter {
                    it.status != com.example.carwash.domain.model.OrderStatus.Anulado
                }
            }
        )
    }

    override fun observeTodayOrders(): Flow<Result<List<Order>>> =
        observeOrdersByDate(LocalDate.now(zoneId))

    override fun observeCachedOrderById(id: String): Flow<Order?> =
        orderDao.observeOrderById(id).map { it?.toDomain() }

    override fun observeCachedOrdersByDate(date: LocalDate): Flow<List<Order>> = flow {
        val companyId = requireCompanyId()
        emitAll(
            orderDao.observeOrdersByRange(
                companyId = companyId,
                startIso = startOfDayIso(date),
                endIso = endOfDayIso(date)
            ).map { orders -> orders.map { it.toDomain() } }
        )
    }

    override suspend fun refreshOrderById(id: String): Result<Order> = runCatching {
        val companyId = requireCompanyId()
        val remote = orderDataSource.getByIdWithDetails(id)
        persistOrder(companyId, remote)
        remote.toDomain()
    }

    override suspend fun refreshOrdersByDate(date: LocalDate): Result<List<Order>> = runCatching {
        val companyId = requireCompanyId()
        refreshOrdersRange(companyId, date, date)
    }

    override suspend fun getOrderById(id: String): Result<Order> = runCatching {
        refreshOrderById(id).getOrThrow()
    }

    override suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> = runCatching {
        val statusStr =
            when (status) {
                OrderStatus.En_Proceso -> "En Proceso"
                OrderStatus.Lavando -> "Lavando"
                OrderStatus.Terminado -> "Terminado"
                OrderStatus.Entregado -> "Entregado"
                OrderStatus.Anulado -> "Anulado"
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
        val companyId = requireCompanyId()
        val subtotal = order.items.sumOf { it.subtotal }
        val orderNumber = generateOrderNumber()

        val photoUrls = if (order.photos.isNotEmpty()) {
            val byteArrays = order.photos.mapNotNull { uri ->
                runCatching { ImageCompressor.compress(contentResolver, uri) }.getOrNull()
            }
            if (byteArrays.isNotEmpty()) {
                try {
                    photoDataSource.uploadPhotos(orderNumber, byteArrays)
                } catch (e: Exception) {
                    Log.e(TAG, "Error uploading photos, continuing without them", e)
                    emptyList()
                }
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }

        val created = orderDataSource.createOrder(
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

        if (order.staffIds.isNotEmpty()) {
            val staffDtos = order.staffIds.mapNotNull { staffId ->
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
            if (staffDtos.isNotEmpty()) {
                orderDataSource.assignStaffBatch(staffDtos)
            }
        }

        orderDataSource.addStatusHistory(
            orderId = created.id,
            status = "En Proceso",
            companyId = companyId,
            changedBy = order.cashierId
        )

        val fullOrder = orderDataSource.getByIdWithDetails(created.id)
        persistOrder(companyId, fullOrder)
        fullOrder.toDomain()
    }

    override suspend fun updateOrderStatus(
        orderId: String,
        status: OrderStatus,
        changedBy: String?,
        note: String?
    ): Result<Unit> = runCatching {
        val companyId = requireCompanyId()
        val statusStr = when (status) {
            OrderStatus.En_Proceso -> "En Proceso"
            OrderStatus.Lavando -> "Lavando"
            OrderStatus.Terminado -> "Terminado"
            OrderStatus.Entregado -> "Entregado"
            OrderStatus.Anulado -> "Anulado"
        }
        orderDataSource.updateStatus(orderId, UpdateOrderStatusDto(status = statusStr))
        orderDataSource.addStatusHistory(
            orderId = orderId,
            status = statusStr,
            companyId = companyId,
            changedBy = changedBy,
            note = note
        )
        refreshOrderById(orderId).getOrThrow()
    }

    override suspend fun cancelOrder(
        orderId: String,
        reason: String,
        changedBy: String?
    ): Result<Unit> = runCatching {
        val companyId = requireCompanyId()
        orderDataSource.updateStatus(
            orderId,
            UpdateOrderStatusDto(status = "Anulado", cancelReason = reason)
        )
        orderDataSource.addStatusHistory(
            orderId = orderId,
            status = "Anulado",
            companyId = companyId,
            changedBy = changedBy,
            note = reason
        )
        refreshOrderById(orderId).getOrThrow()
    }

    override suspend fun registerPayment(
        orderId: String,
        paymentMethod: String,
        paymentStatus: PaymentStatus
    ): Result<Unit> = runCatching {
        val companyId = requireCompanyId()
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
        refreshOrderById(orderId).getOrThrow()
    }

    override suspend fun getOrdersByPeriod(period: OrderPeriod): Result<List<Order>> = runCatching {
        val companyId = requireCompanyId()
        val (startDate, endDate) = periodRange(period)
        refreshOrdersRange(companyId, startDate, endDate)
    }

    override fun observeOrdersByPeriod(period: OrderPeriod): Flow<Result<List<Order>>> = flow {
        val companyId = requireCompanyId()
        val (startDate, endDate) = periodRange(period)
        repositoryScope.launch {
            refreshOrdersRange(companyId, startDate, endDate)
        }
        emitAll(
            orderDao.observeOrdersByRange(
                companyId = companyId,
                startIso = startOfDayIso(startDate),
                endIso = endOfDayIso(endDate)
            ).map { orders -> Result.success(orders.map { it.toDomain() }) }
        )
    }.catch { e -> emit(Result.failure(e)) }

    override fun observeOrdersByDate(date: LocalDate): Flow<Result<List<Order>>> = flow {
        val companyId = requireCompanyId()
        repositoryScope.launch {
            refreshOrdersRange(companyId, date, date)
            if (date == LocalDate.now(zoneId)) {
                orderDataSource.observeOrdersByPeriod(startOfDayIso(date), endOfDayIso(date))
                    .collect { remoteOrders ->
                        replaceOrdersRange(companyId, date, date, remoteOrders)
                    }
            }
        }
        emitAll(
            orderDao.observeOrdersByRange(
                companyId = companyId,
                startIso = startOfDayIso(date),
                endIso = endOfDayIso(date)
            ).map { orders -> Result.success(orders.map { it.toDomain() }) }
        )
    }.catch { e -> emit(Result.failure(e)) }

    override suspend fun getOrdersByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<List<Order>> = runCatching {
        val companyId = requireCompanyId()
        refreshOrdersRange(companyId, startDate, endDate)
    }

    override suspend fun updateOrderStaff(
        orderId: String,
        toAdd: List<String>,
        toRemove: List<String>
    ): Result<Unit> = runCatching {
        val companyId = requireCompanyId()
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
        if (staffDtos.isNotEmpty()) {
            orderDataSource.assignStaffBatch(staffDtos)
        }
        refreshOrderById(orderId).getOrThrow()
    }

    override suspend fun updateOrderItems(
        orderId: String,
        toAdd: List<OrderItemRequest>,
        toRemove: List<String>
    ): Result<Unit> = runCatching {
        val companyId = requireCompanyId()
        toRemove.forEach { itemId -> orderDataSource.deleteOrderItem(itemId) }
        if (toAdd.isNotEmpty()) {
            orderDataSource.addOrderItems(
                toAdd.map { item ->
                    CreateOrderItemDto(
                        orderId = orderId,
                        companyId = companyId,
                        serviceId = item.serviceId,
                        serviceName = item.serviceName,
                        unitPrice = item.unitPrice,
                        quantity = item.quantity,
                        subtotal = item.subtotal
                    )
                }
            )
        }
        val allItems = orderDataSource.getOrderItems(orderId)
        val newSubtotal = allItems.sumOf { it.subtotal }
        orderDataSource.updateTotals(orderId, newSubtotal, 0.0, newSubtotal)
        refreshOrderById(orderId).getOrThrow()
    }

    override suspend fun getPaymentMethods(): Result<List<PaymentMethod>> = runCatching {
        paymentMethodDataSource.getActive().map { it.toDomain() }
    }

    override suspend fun deliverOrder(
        orderId: String,
        paymentMethod: String,
        newPhotoUris: List<Uri>
    ): Result<Unit> = runCatching {
        val companyId = requireCompanyId()

        if (newPhotoUris.isNotEmpty()) {
            val order = orderDataSource.getByIdWithDetails(orderId).toDomain()
            val byteArrays = newPhotoUris.mapNotNull { uri ->
                runCatching { ImageCompressor.compress(contentResolver, uri) }.getOrNull()
            }
            val newUrls = if (byteArrays.isNotEmpty()) {
                try {
                    photoDataSource.uploadPhotos(order.orderNumber, byteArrays)
                } catch (e: Exception) {
                    Log.e(TAG, "Error uploading delivery photos", e)
                    emptyList()
                }
            } else {
                emptyList()
            }

            if (newUrls.isNotEmpty()) {
                val merged = order.photos + newUrls
                orderDataSource.updatePhotos(orderId, merged)
            }
        }

        orderDataSource.updateStatus(
            orderId,
            UpdateOrderStatusDto(
                status = "Entregado",
                paymentStatus = "pagado",
                paymentMethod = paymentMethod
            )
        )

        orderDataSource.addStatusHistory(
            orderId = orderId,
            status = "Entregado",
            companyId = companyId,
            note = "Entrega confirmada · Pago: $paymentMethod"
        )
        refreshOrderById(orderId).getOrThrow()
    }

    private suspend fun refreshOrdersRange(
        companyId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Order> {
        val remoteOrders = orderDataSource.getAllForPeriod(startOfDayIso(startDate), endOfDayIso(endDate))
        replaceOrdersRange(companyId, startDate, endDate, remoteOrders)
        return remoteOrders.map { it.toDomain() }
    }

    private suspend fun replaceOrdersRange(
        companyId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        remoteOrders: List<com.example.carwash.data.remote.dto.OrderWithDetailsDto>
    ) {
        val graphs = remoteOrders.map { it.toCachedOrderGraph(companyId) }
        orderDao.replaceOrdersForRange(
            companyId = companyId,
            startIso = startOfDayIso(startDate),
            endIso = endOfDayIso(endDate),
            orders = graphs.map { it.order },
            customers = graphs.mapNotNull { it.customer }.distinctBy { it.id },
            vehicles = graphs.mapNotNull { it.vehicle }.distinctBy { it.id },
            items = graphs.flatMap { it.items },
            staff = graphs.flatMap { it.staff },
            statusHistory = graphs.flatMap { it.statusHistory }
        )
    }

    private suspend fun persistOrder(
        companyId: String,
        order: com.example.carwash.data.remote.dto.OrderWithDetailsDto
    ) {
        val graph = order.toCachedOrderGraph(companyId)
        orderDao.upsertOrderGraph(
            order = graph.order,
            customer = graph.customer,
            vehicle = graph.vehicle,
            items = graph.items,
            staff = graph.staff,
            statusHistory = graph.statusHistory
        )
    }

    private fun periodRange(period: OrderPeriod): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now(zoneId)
        return when (period) {
            OrderPeriod.Today -> today to today
            OrderPeriod.ThisWeek -> today.with(DayOfWeek.MONDAY) to today
            OrderPeriod.ThisMonth -> today.withDayOfMonth(1) to today
        }
    }

    private fun startOfDayIso(date: LocalDate): String =
        date.atStartOfDay(zoneId).toOffsetDateTime().toString()

    private fun endOfDayIso(date: LocalDate): String =
        date.atTime(LocalTime.of(23, 59, 59)).atZone(zoneId).toOffsetDateTime().toString()

    private fun generateOrderNumber(): String =
        "ORD-${System.currentTimeMillis().toString().takeLast(8)}"

    private companion object {
        const val TAG = "OrderRepositoryImpl"
    }
}
