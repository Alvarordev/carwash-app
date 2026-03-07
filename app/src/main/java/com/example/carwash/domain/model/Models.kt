package com.example.carwash.domain.model

import java.time.LocalDate
import java.time.OffsetDateTime

data class Company(
    val id: String,
    val name: String,
    val slug: String,
    val status: EntityStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

data class Customer(
    val id: String,
    val firstName: String,
    val lastName: String,
    val docType: DocumentType? = null,
    val docNumber: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val status: EntityStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) {
    val fullName: String get() = "$firstName $lastName"
}

data class VehicleType(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: EntityStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

data class Vehicle(
    val id: String,
    val plate: String,
    val color: String,
    val brand: String,
    val model: String? = null,
    val vehicleTypeId: String,
    val vehicleType: VehicleType? = null,  // expandido cuando se hace join
    val status: EntityStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) {
    val displayName: String get() = "$brand ${model ?: ""} - $plate".trim()
}

data class StaffMember(
    val id: String,
    val firstName: String,
    val lastName: String,
    val docType: DocumentType? = null,
    val docNumber: String? = null,
    val role: StaffRole,
    val phone: String? = null,
    val email: String? = null,
    val status: EntityStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) {
    val fullName: String get() = "$firstName $lastName"
}

data class Service(
    val id: String,
    val name: String,
    val description: String? = null,
    val category: ServiceCategory,
    val status: EntityStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val color: String? = null,
    val icon: String? = null
)

data class ServicePricing(
    val id: String,
    val serviceId: String,
    val vehicleTypeId: String,
    val price: Double,
    val status: EntityStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

data class InventoryItem(
    val id: String,
    val name: String,
    val description: String? = null,
    val unit: String,
    val quantity: Double,
    val minQuantity: Double,
    val status: EntityStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) {
    val isLowStock: Boolean get() = quantity <= minQuantity
}

data class Promotion(
    val id: String,
    val name: String,
    val description: String? = null,
    val discountType: DiscountType,
    val discountValue: Double,
    val scope: PromotionScope,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val status: EntityStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) {
    val isActive: Boolean
        get() {
            if (status != EntityStatus.Active) return false
            val today = LocalDate.now()
            val afterStart = startDate?.let { !today.isBefore(it) } ?: true
            val beforeEnd = endDate?.let { !today.isAfter(it) } ?: true
            return afterStart && beforeEnd
        }
}