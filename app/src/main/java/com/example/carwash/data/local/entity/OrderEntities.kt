package com.example.carwash.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val companyId: String,
    val orderNumber: String,
    val customerId: String? = null,
    val vehicleId: String? = null,
    val cashierId: String? = null,
    val subtotal: Double,
    val discounts: Double,
    val total: Double,
    val status: String,
    val paymentStatus: String? = null,
    val paymentMethod: String? = null,
    val cancelReason: String? = null,
    val notes: String? = null,
    val photos: List<String>,
    val createdAt: String,
    val updatedAt: String
)

@Entity(tableName = "order_customers")
data class OrderCustomerEntity(
    @PrimaryKey val id: String,
    val companyId: String,
    val firstName: String,
    val lastName: String,
    val docType: String? = null,
    val docNumber: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

@Entity(tableName = "order_vehicles")
data class OrderVehicleEntity(
    @PrimaryKey val id: String,
    val companyId: String,
    val plate: String,
    val color: String,
    val brand: String,
    val model: String? = null,
    val vehicleTypeId: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

@Entity(tableName = "order_items", primaryKeys = ["id"])
data class OrderItemEntity(
    val id: String,
    val orderId: String,
    val companyId: String,
    val serviceId: String? = null,
    val serviceName: String,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double,
    val createdAt: String,
    val serviceColor: String? = null,
    val serviceIcon: String? = null
)

@Entity(tableName = "order_staff", primaryKeys = ["id"])
data class OrderStaffEntity(
    val id: String,
    val orderId: String,
    val companyId: String,
    val staffId: String? = null,
    val staffName: String,
    val roleSnapshot: String? = null,
    val createdAt: String
)

@Entity(tableName = "order_status_history", primaryKeys = ["id"])
data class OrderStatusHistoryEntity(
    val id: String,
    val orderId: String,
    val status: String,
    val changedBy: String? = null,
    val note: String? = null,
    val createdAt: String
)

data class OrderWithRelationsEntity(
    @Embedded val order: OrderEntity,
    @Relation(parentColumn = "customerId", entityColumn = "id")
    val customer: OrderCustomerEntity? = null,
    @Relation(parentColumn = "vehicleId", entityColumn = "id")
    val vehicle: OrderVehicleEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val items: List<OrderItemEntity> = emptyList(),
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val staff: List<OrderStaffEntity> = emptyList(),
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val statusHistory: List<OrderStatusHistoryEntity> = emptyList()
)
