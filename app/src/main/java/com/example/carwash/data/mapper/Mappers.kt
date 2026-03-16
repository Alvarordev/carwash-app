package com.example.carwash.data.mapper

import com.example.carwash.data.remote.dto.CompanyDto
import com.example.carwash.data.remote.dto.CustomerDto
import com.example.carwash.data.remote.dto.InventoryItemDto
import com.example.carwash.data.remote.dto.OrderDto
import com.example.carwash.data.remote.dto.PaymentMethodDto
import com.example.carwash.data.remote.dto.OrderItemDto
import com.example.carwash.data.remote.dto.OrderStaffDto
import com.example.carwash.data.remote.dto.OrderStatusHistoryDto
import com.example.carwash.data.remote.dto.OrderWithDetailsDto
import com.example.carwash.data.remote.dto.PromotionDto
import com.example.carwash.data.remote.dto.ServiceCategoryDto
import com.example.carwash.data.remote.dto.ServiceDto
import com.example.carwash.data.remote.dto.ServicePricingDto
import com.example.carwash.data.remote.dto.StaffMemberDto
import com.example.carwash.data.remote.dto.UserProfileDto
import com.example.carwash.data.remote.dto.VehicleDto
import com.example.carwash.data.remote.dto.VehicleTypeDto
import com.example.carwash.domain.model.*
import java.time.LocalDate
import java.time.OffsetDateTime

internal fun String.toOffsetDateTime(): OffsetDateTime =
    OffsetDateTime.parse(this)

internal fun String?.toLocalDate(): LocalDate? =
    this?.let { LocalDate.parse(it) }

internal fun String.toEntityStatus(): EntityStatus =
    if (this == "active") EntityStatus.Active else EntityStatus.Inactive

internal fun String?.toDocumentType(): DocumentType? = when (this) {
    "dni" -> DocumentType.Dni
    "carnet_extranjeria" -> DocumentType.CarnetExtranjeria
    "pasaporte" -> DocumentType.Pasaporte
    else -> null
}

internal fun String.toUserProfileRole(): UserProfileRole = when (this) {
    "super_admin" -> UserProfileRole.SuperAdmin
    "admin" -> UserProfileRole.Admin
    else -> UserProfileRole.Operator
}

internal fun String.toStaffRole(): StaffRole = when (this) {
    "admin" -> StaffRole.Admin
    "washer" -> StaffRole.Washer
    "cashier" -> StaffRole.Cashier
    "supervisor" -> StaffRole.Supervisor
    else -> StaffRole.Washer
}

internal fun String?.toStaffRoleOrNull(): StaffRole? = this?.let {
    runCatching { it.toStaffRole() }.getOrNull()
}

internal fun String.toDiscountType(): DiscountType = when (this) {
    "percentage" -> DiscountType.Percentage
    else -> DiscountType.Fixed
}

internal fun String.toPromotionScope(): PromotionScope = when (this) {
    "service" -> PromotionScope.Service
    "vehicleType" -> PromotionScope.VehicleType
    else -> PromotionScope.All
}

internal fun String.toOrderStatus(): OrderStatus = when (this) {
    "En Proceso" -> OrderStatus.EnProceso
    "Lavando" -> OrderStatus.Lavando
    "Terminado" -> OrderStatus.Terminado
    "Entregado" -> OrderStatus.Entregado
    "Anulado" -> OrderStatus.Anulado
    "Cancelado" -> OrderStatus.Anulado
    else -> OrderStatus.EnProceso
}

internal fun String?.toPaymentStatus(): PaymentStatus? = when (this) {
    "pendiente" -> PaymentStatus.Pendiente
    "pagado" -> PaymentStatus.Pagado
    "parcial" -> PaymentStatus.Parcial
    else -> null
}

fun CompanyDto.toDomain() = Company(
    id = id,
    name = name,
    slug = slug,
    status = status.toEntityStatus(),
    createdAt = createdAt.toOffsetDateTime(),
    updatedAt = updatedAt.toOffsetDateTime()
)

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
    id = id.orEmpty(),
    plate = plate,
    color = color,
    brand = brand,
    model = model,
    vehicleTypeId = vehicleTypeId,
    status = status.toEntityStatus(),
    createdAt = createdAt?.toOffsetDateTime() ?: OffsetDateTime.now(),
    updatedAt = updatedAt?.toOffsetDateTime() ?: OffsetDateTime.now()
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

fun ServiceCategoryDto.toDomain() = ServiceCategory(
    id = id,
    name = name,
    description = description,
    color = color,
    icon = icon
)

fun ServiceDto.toDomain() = Service(
    id = id,
    name = name,
    description = description,
    categoryId = categoryId,
    category = serviceCategory?.toDomain(),
    status = status.toEntityStatus(),
    createdAt = createdAt.toOffsetDateTime(),
    updatedAt = updatedAt.toOffsetDateTime(),
    color = color,
    icon = icon
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

fun PaymentMethodDto.toDomain() = PaymentMethod(id = id, name = name)

fun UserProfileDto.toDomain() = UserProfile(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    role = role.toUserProfileRole()
)

fun OrderItemDto.toDomain() = OrderItem(
    id = id,
    orderId = orderId,
    serviceId = serviceId,
    serviceName = serviceName,
    unitPrice = unitPrice,
    quantity = quantity,
    subtotal = subtotal,
    createdAt = createdAt.toOffsetDateTime(),
    serviceColor = services?.color,
    serviceIcon = services?.icon
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
    photos = photos,
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
    photos = photos,
    createdAt = createdAt.toOffsetDateTime(),
    updatedAt = updatedAt.toOffsetDateTime(),
    customer = customers?.toDomain(),
    vehicle = vehicles?.toDomain(),
    items = orderItems.map { it.toDomain() },
    staff = orderStaff.map { it.toDomain() },
    statusHistory = orderStatusHistory.map { it.toDomain() }
)
