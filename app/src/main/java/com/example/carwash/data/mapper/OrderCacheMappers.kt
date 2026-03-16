package com.example.carwash.data.mapper

import com.example.carwash.data.local.entity.OrderCustomerEntity
import com.example.carwash.data.local.entity.OrderEntity
import com.example.carwash.data.local.entity.OrderItemEntity
import com.example.carwash.data.local.entity.OrderStaffEntity
import com.example.carwash.data.local.entity.OrderStatusHistoryEntity
import com.example.carwash.data.local.entity.OrderVehicleEntity
import com.example.carwash.data.local.entity.OrderWithRelationsEntity
import com.example.carwash.data.remote.dto.CustomerDto
import com.example.carwash.data.remote.dto.OrderItemDto
import com.example.carwash.data.remote.dto.OrderStaffDto
import com.example.carwash.data.remote.dto.OrderStatusHistoryDto
import com.example.carwash.data.remote.dto.OrderWithDetailsDto
import com.example.carwash.data.remote.dto.VehicleDto
import com.example.carwash.domain.model.Customer
import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderItem
import com.example.carwash.domain.model.OrderStaff
import com.example.carwash.domain.model.OrderStatusHistory
import com.example.carwash.domain.model.Vehicle
import java.time.OffsetDateTime

internal data class CachedOrderGraph(
    val order: OrderEntity,
    val customer: OrderCustomerEntity?,
    val vehicle: OrderVehicleEntity?,
    val items: List<OrderItemEntity>,
    val staff: List<OrderStaffEntity>,
    val statusHistory: List<OrderStatusHistoryEntity>
)

internal fun OrderWithDetailsDto.toCachedOrderGraph(companyId: String): CachedOrderGraph = CachedOrderGraph(
    order = OrderEntity(
        id = id,
        companyId = this.companyIdFromDto(companyId),
        orderNumber = orderNumber,
        customerId = customerId,
        vehicleId = vehicleId,
        cashierId = cashierId,
        subtotal = subtotal,
        discounts = discounts,
        total = total,
        status = status,
        paymentStatus = paymentStatus,
        paymentMethod = paymentMethod,
        cancelReason = cancelReason,
        notes = notes,
        photos = photos,
        createdAt = createdAt,
        updatedAt = updatedAt
    ),
    customer = customers?.toEntity(companyId),
    vehicle = vehicles?.toEntity(companyId),
    items = orderItems.map { it.toEntity(companyId) },
    staff = orderStaff.map { it.toEntity(companyId) },
    statusHistory = orderStatusHistory.map { it.toEntity() }
)

internal fun OrderWithRelationsEntity.toDomain(): Order = Order(
    id = order.id,
    orderNumber = order.orderNumber,
    customerId = order.customerId,
    vehicleId = order.vehicleId,
    cashierId = order.cashierId,
    subtotal = order.subtotal,
    discounts = order.discounts,
    total = order.total,
    status = order.status.toOrderStatus(),
    paymentStatus = order.paymentStatus.toPaymentStatus(),
    paymentMethod = order.paymentMethod,
    cancelReason = order.cancelReason,
    notes = order.notes,
    photos = order.photos,
    createdAt = order.createdAt.toOffsetDateTime(),
    updatedAt = order.updatedAt.toOffsetDateTime(),
    customer = customer?.toDomain(),
    vehicle = vehicle?.toDomain(),
    items = items.map { it.toDomain() },
    staff = staff.map { it.toDomain() },
    statusHistory = statusHistory.map { it.toDomain() }
)

private fun CustomerDto.toEntity(companyId: String) = OrderCustomerEntity(
    id = id,
    companyId = this.companyIdFromDto(companyId),
    firstName = firstName,
    lastName = lastName,
    docType = docType,
    docNumber = docNumber,
    phone = phone,
    email = email,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun VehicleDto.toEntity(companyId: String) = OrderVehicleEntity(
    id = id.orEmpty(),
    companyId = this.companyIdFromDto(companyId),
    plate = plate,
    color = color,
    brand = brand,
    model = model,
    vehicleTypeId = vehicleTypeId,
    status = status,
    createdAt = createdAt ?: OffsetDateTime.now().toString(),
    updatedAt = updatedAt ?: OffsetDateTime.now().toString()
)

private fun OrderItemDto.toEntity(companyId: String) = OrderItemEntity(
    id = id,
    orderId = orderId,
    companyId = this.companyIdFromDto(companyId),
    serviceId = serviceId,
    serviceName = serviceName,
    unitPrice = unitPrice,
    quantity = quantity,
    subtotal = subtotal,
    createdAt = createdAt,
    serviceColor = services?.color,
    serviceIcon = services?.icon
)

private fun OrderStaffDto.toEntity(companyId: String) = OrderStaffEntity(
    id = id,
    orderId = orderId,
    companyId = this.companyIdFromDto(companyId),
    staffId = staffId,
    staffName = staffName,
    roleSnapshot = roleSnapshot,
    createdAt = createdAt
)

private fun OrderStatusHistoryDto.toEntity() = OrderStatusHistoryEntity(
    id = id,
    orderId = orderId,
    status = status,
    changedBy = changedBy,
    note = note,
    createdAt = createdAt
)

private fun OrderCustomerEntity.toDomain() = Customer(
    id = id,
    firstName = firstName,
    lastName = lastName,
    docType = docType.toDocumentType(),
    docNumber = docNumber,
    phone = phone,
    email = email,
    status = status.toEntityStatus(),
    createdAt = createdAt.toOffsetDateTime(),
    updatedAt = updatedAt.toOffsetDateTime()
)

private fun OrderVehicleEntity.toDomain() = Vehicle(
    id = id,
    plate = plate,
    color = color,
    brand = brand,
    model = model,
    vehicleTypeId = vehicleTypeId,
    status = status.toEntityStatus(),
    createdAt = createdAt.toOffsetDateTime(),
    updatedAt = updatedAt.toOffsetDateTime()
)

private fun OrderItemEntity.toDomain() = OrderItem(
    id = id,
    orderId = orderId,
    serviceId = serviceId,
    serviceName = serviceName,
    unitPrice = unitPrice,
    quantity = quantity,
    subtotal = subtotal,
    createdAt = createdAt.toOffsetDateTime(),
    serviceColor = serviceColor,
    serviceIcon = serviceIcon
)

private fun OrderStaffEntity.toDomain() = OrderStaff(
    id = id,
    orderId = orderId,
    staffId = staffId,
    staffName = staffName,
    roleSnapshot = roleSnapshot.toStaffRoleOrNull(),
    createdAt = createdAt.toOffsetDateTime()
)

private fun OrderStatusHistoryEntity.toDomain() = OrderStatusHistory(
    id = id,
    orderId = orderId,
    status = status.toOrderStatus(),
    changedBy = changedBy,
    note = note,
    createdAt = createdAt.toOffsetDateTime()
)

private fun OrderWithDetailsDto.companyIdFromDto(fallback: String): String = companyId ?: fallback

private fun CustomerDto.companyIdFromDto(fallback: String): String = companyId ?: fallback

private fun VehicleDto.companyIdFromDto(fallback: String): String = companyId ?: fallback

private fun OrderItemDto.companyIdFromDto(fallback: String): String = companyId ?: fallback

private fun OrderStaffDto.companyIdFromDto(fallback: String): String = companyId ?: fallback
