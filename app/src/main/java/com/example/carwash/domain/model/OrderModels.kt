package com.example.carwash.domain.model

import java.time.OffsetDateTime

data class OrderItem(
        val id: String,
        val orderId: String,
        val serviceId: String? = null,
        val serviceName: String,
        val unitPrice: Double,
        val quantity: Int,
        val subtotal: Double,
        val createdAt: OffsetDateTime
)

data class OrderStaff(
        val id: String,
        val orderId: String,
        val staffId: String? = null,
        val staffName: String,
        val roleSnapshot: StaffRole? = null,
        val createdAt: OffsetDateTime
)

data class OrderStatusHistory(
        val id: String,
        val orderId: String,
        val status: OrderStatus,
        val changedBy: String? = null,
        val note: String? = null,
        val createdAt: OffsetDateTime
)

data class OrderAttachment(
        val id: String,
        val orderId: String,
        val url: String,
        val createdAt: OffsetDateTime
)

data class Order(
        val id: String,
        val orderNumber: String,
        val customerId: String? = null,
        val vehicleId: String? = null,
        val cashierId: String? = null,
        val subtotal: Double,
        val discounts: Double,
        val total: Double,
        val status: OrderStatus,
        val paymentStatus: PaymentStatus? = null,
        val paymentMethod: String? = null,
        val cancelReason: String? = null,
        val notes: String? = null,
        val photos: List<String> = emptyList(),
        val createdAt: OffsetDateTime,
        val updatedAt: OffsetDateTime,
        val customer: Customer? = null,
        val vehicle: Vehicle? = null,
        val items: List<OrderItem> = emptyList(),
        val staff: List<OrderStaff> = emptyList()
) {
    val isPending: Boolean
        get() = status == OrderStatus.EnProceso
    val isInProgress: Boolean
        get() = status == OrderStatus.EnProceso
    val isCompleted: Boolean
        get() = status == OrderStatus.Entregado
    val isCancelled: Boolean
        get() = status == OrderStatus.Cancelado
    val isPaid: Boolean
        get() = paymentStatus == PaymentStatus.Pagado
    val canChangeStatus: Boolean
        get() = !isCompleted && !isCancelled
}

data class OrderItemRequest(
        val serviceId: String? = null,
        val serviceName: String,
        val unitPrice: Double,
        val quantity: Int = 1
) {
    val subtotal: Double
        get() = unitPrice * quantity
}

data class CreateOrderRequest(
        val customerId: String? = null,
        val vehicleId: String? = null,
        val cashierId: String? = null,
        val items: List<OrderItemRequest>,
        val staffIds: List<String> = emptyList(),
        val notes: String? = null,
        val photos: List<android.net.Uri> = emptyList()
)
