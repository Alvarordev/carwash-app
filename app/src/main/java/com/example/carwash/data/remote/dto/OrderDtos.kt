package com.example.carwash.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: String,
    @SerialName("order_number") val orderNumber: String,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("vehicle_id") val vehicleId: String? = null,
    @SerialName("cashier_id") val cashierId: String? = null,
    val subtotal: Double = 0.0,      // ← agrega = 0.0 como default
    val discounts: Double = 0.0,     // ← agrega = 0.0 como default
    val total: Double = 0.0,         // ← agrega = 0.0 como default
    val status: String,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("cancel_reason") val cancelReason: String? = null,
    val notes: String? = null,
    val photos: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class OrderWithDetailsDto(
    val id: String,
    @SerialName("order_number") val orderNumber: String,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("vehicle_id") val vehicleId: String? = null,
    @SerialName("cashier_id") val cashierId: String? = null,
    val subtotal: Double = 0.0,      // ← agrega = 0.0 como default
    val discounts: Double = 0.0,     // ← agrega = 0.0 como default
    val total: Double = 0.0,         // ← agrega = 0.0 como default
    val status: String,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("cancel_reason") val cancelReason: String? = null,
    val notes: String? = null,
    val photos: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    // Relaciones embebidas (PostgREST join)
    val customers: CustomerDto? = null,
    val vehicles: VehicleDto? = null,
    @SerialName("order_items") val orderItems: List<OrderItemDto> = emptyList(),
    @SerialName("order_staff") val orderStaff: List<OrderStaffDto> = emptyList()
)

@Serializable
data class OrderItemDto(
    val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("service_id") val serviceId: String? = null,
    @SerialName("service_name") val serviceName: String,
    @SerialName("unit_price") val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class OrderStaffDto(
    val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("staff_id") val staffId: String? = null,
    @SerialName("staff_name") val staffName: String,
    @SerialName("role_snapshot") val roleSnapshot: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class OrderStatusHistoryDto(
    val id: String,
    @SerialName("order_id") val orderId: String,
    val status: String,
    @SerialName("changed_by") val changedBy: String? = null,
    val note: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class OrderAttachmentDto(
    val id: String,
    @SerialName("order_id") val orderId: String,
    val url: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class CreateOrderDto(
    @SerialName("order_number") val orderNumber: String,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("vehicle_id") val vehicleId: String? = null,
    @SerialName("cashier_id") val cashierId: String? = null,
    val subtotal: Double = 0.0,
    val discounts: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "Pendiente",
    @SerialName("payment_method") val paymentMethod: String? = null,
    val photos: List<String> = emptyList(),
    val notes: String? = null
)

@Serializable
data class UpdateOrderStatusDto(
    val status: String,
    @SerialName("cancel_reason") val cancelReason: String? = null,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null
)

@Serializable
data class CreateOrderItemDto(
    @SerialName("order_id") val orderId: String,
    @SerialName("service_id") val serviceId: String? = null,
    @SerialName("service_name") val serviceName: String,
    @SerialName("unit_price") val unitPrice: Double,
    val quantity: Int = 1,
    val subtotal: Double
)

@Serializable
data class CreateOrderStaffDto(
    @SerialName("order_id") val orderId: String,
    @SerialName("staff_id") val staffId: String? = null,
    @SerialName("staff_name") val staffName: String,
    @SerialName("role_snapshot") val roleSnapshot: String? = null
)