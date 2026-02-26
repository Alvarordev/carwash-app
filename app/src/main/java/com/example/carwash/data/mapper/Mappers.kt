package com.example.carwash.data.mapper

import com.example.carwash.data.remote.dto.CustomerDto
import com.example.carwash.data.remote.dto.InventoryItemDto
import com.example.carwash.data.remote.dto.OrderDto
import com.example.carwash.data.remote.dto.OrderItemDto
import com.example.carwash.data.remote.dto.OrderStaffDto
import com.example.carwash.data.remote.dto.OrderStatusHistoryDto
import com.example.carwash.data.remote.dto.OrderWithDetailsDto
import com.example.carwash.data.remote.dto.PromotionDto
import com.example.carwash.data.remote.dto.ServiceDto
import com.example.carwash.data.remote.dto.ServicePricingDto
import com.example.carwash.data.remote.dto.StaffMemberDto
import com.example.carwash.data.remote.dto.VehicleDto
import com.example.carwash.data.remote.dto.VehicleTypeDto
import com.example.carwash.domain.model.*
import java.time.LocalDate
import java.time.OffsetDateTime

// ──────────────────────────────────────────────
// Helpers de conversión de tipos
// ──────────────────────────────────────────────

private fun String.toOffsetDateTime(): OffsetDateTime =
    OffsetDateTime.parse(this)

private fun String?.toLocalDate(): LocalDate? =
    this?.let { LocalDate.parse(it) }

private fun String.toEntityStatus(): EntityStatus =
    if (this == "active") EntityStatus.Active else EntityStatus.Inactive

private fun String?.toDocumentType(): DocumentType? = when (this) {
    "dni" -> DocumentType.Dni
    "carnet_extranjeria" -> DocumentType.CarnetExtranjeria
    "pasaporte" -> DocumentType.Pasaporte
    else -> null
}

private fun String.toStaffRole(): StaffRole = when (this) {
    "admin" -> StaffRole.Admin
    "washer" -> StaffRole.Washer
    "cashier" -> StaffRole.Cashier
    "supervisor" -> StaffRole.Supervisor
    else -> StaffRole.Washer
}

private fun String?.toStaffRoleOrNull(): StaffRole? = this?.let {
    runCatching { it.toStaffRole() }.getOrNull()
}

private fun String.toServiceCategory(): ServiceCategory = when (this) {
    "exterior" -> ServiceCategory.Exterior
    "interior" -> ServiceCategory.Interior
    "detalle" -> ServiceCategory.Detalle
    "añadido" -> ServiceCategory.Aniadido
    else -> ServiceCategory.Exterior
}

private fun String.toDiscountType(): DiscountType = when (this) {
    "percentage" -> DiscountType.Percentage
    else -> DiscountType.Fixed
}

private fun String.toPromotionScope(): PromotionScope = when (this) {
    "service" -> PromotionScope.Service
    "vehicleType" -> PromotionScope.VehicleType
    else -> PromotionScope.All
}

private fun String.toOrderStatus(): OrderStatus = when (this) {
    "Pendiente" -> OrderStatus.Pendiente
    "En Proceso" -> OrderStatus.EnProceso
    "Cancelado" -> OrderStatus.Cancelado
    "Entregado" -> OrderStatus.Entregado
    else -> OrderStatus.Pendiente
}

private fun String?.toPaymentStatus(): PaymentStatus? = when (this) {
    "pendiente" -> PaymentStatus.Pendiente
    "pagado" -> PaymentStatus.Pagado
    "parcial" -> PaymentStatus.Parcial
    else -> null
}

// ──────────────────────────────────────────────
// Extensiones de mapeo DTO → Domain
// ──────────────────────────────────────────────

fun CustomerDto.toDomain() = Customer(
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

fun VehicleTypeDto.toDomain() = VehicleType(
    id = id,
    name = name,
    description = description,
    status = status.toEntityStatus(),
    createdAt = createdAt.toOffsetDateTime(),
    updatedAt = updatedAt.toOffsetDateTime()
)

fun VehicleDto.toDomain() = Vehicle(
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

fun StaffMemberDto.toDomain() = StaffMember(
    id = id,
    firstName = firstName,
    lastName = lastName,
    docType = docType.toDocumentType(),
    docNumber = docNumber,
    role = role.toStaffRole(),
    phone = phone,
    email = email,
    status = status.toEntityStatus(),
    createdAt = createdAt.toOffsetDateTime(),
    updatedAt = updatedAt.toOffsetDateTime()
)

fun ServiceDto.toDomain() = Service(
    id = id,
    name = name,
    description = description,
    category = category.toServiceCategory(),
    status = status.toEntityStatus(),
    createdAt = createdAt.toOffsetDateTime(),
    updatedAt = updatedAt.toOffsetDateTime()
)

fun ServicePricingDto.toDomain() = ServicePricing(
    id = id,
    serviceId = serviceId,
    vehicleTypeId = vehicleTypeId,
    price = price,
    status = status.toEntityStatus(),
    createdAt = createdAt.toOffsetDateTime(),
    updatedAt = updatedAt.toOffsetDateTime()
)

fun InventoryItemDto.toDomain() = InventoryItem(
    id = id,
    name = name,
    description = description,
    unit = unit,
    quantity = quantity,
    minQuantity = minQuantity,
    status = status.toEntityStatus(),
    createdAt = createdAt.toOffsetDateTime(),
    updatedAt = updatedAt.toOffsetDateTime()
)

fun PromotionDto.toDomain() = Promotion(
    id = id,
    name = name,
    description = description,
    discountType = discountType.toDiscountType(),
    discountValue = discountValue,
    scope = scope.toPromotionScope(),
    startDate = startDate.toLocalDate(),
    endDate = endDate.toLocalDate(),
    status = status.toEntityStatus(),
    createdAt = createdAt.toOffsetDateTime(),
    updatedAt = updatedAt.toOffsetDateTime()
)

fun OrderItemDto.toDomain() = OrderItem(
    id = id,
    orderId = orderId,
    serviceId = serviceId,
    serviceName = serviceName,
    unitPrice = unitPrice,
    quantity = quantity,
    subtotal = subtotal,
    createdAt = createdAt.toOffsetDateTime()
)

fun OrderStaffDto.toDomain() = OrderStaff(
    id = id,
    orderId = orderId,
    staffId = staffId,
    staffName = staffName,
    roleSnapshot = roleSnapshot.toStaffRoleOrNull(),
    createdAt = createdAt.toOffsetDateTime()
)

fun OrderStatusHistoryDto.toDomain() = OrderStatusHistory(
    id = id,
    orderId = orderId,
    status = status.toOrderStatus(),
    changedBy = changedBy,
    note = note,
    createdAt = createdAt.toOffsetDateTime()
)

fun OrderDto.toDomain() = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = customerId,
    vehicleId = vehicleId,
    cashierId = cashierId,
    subtotal = subtotal,
    discounts = discounts,
    total = total,
    status = status.toOrderStatus(),
    paymentStatus = paymentStatus.toPaymentStatus(),
    paymentMethod = paymentMethod,
    cancelReason = cancelReason,
    notes = notes,
    createdAt = createdAt.toOffsetDateTime(),
    updatedAt = updatedAt.toOffsetDateTime()
)

fun OrderWithDetailsDto.toDomain() = Order(
    id = id,
    orderNumber = orderNumber,
    customerId = customerId,
    vehicleId = vehicleId,
    cashierId = cashierId,
    subtotal = subtotal,
    discounts = discounts,
    total = total,
    status = status.toOrderStatus(),
    paymentStatus = paymentStatus.toPaymentStatus(),
    paymentMethod = paymentMethod,
    cancelReason = cancelReason,
    notes = notes,
    createdAt = createdAt.toOffsetDateTime(),
    updatedAt = updatedAt.toOffsetDateTime(),
    customer = customers?.toDomain(),
    vehicle = vehicles?.toDomain(),
    items = orderItems.map { it.toDomain() },
    staff = orderStaff.map { it.toDomain() }
)