package com.example.carwash.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CompanyDto(
    val id: String,
    val name: String,
    val slug: String,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class VehicleTypeDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("company_id") val companyId: String? = null
)


@Serializable
data class CustomerDto(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("doc_type") val docType: String? = null,
    @SerialName("doc_number") val docNumber: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("company_id") val companyId: String? = null
)

@Serializable
data class CustomerInsertDto(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val phone: String? = null,
    @SerialName("doc_type") val docType: String? = null,
    @SerialName("doc_number") val docNumber: String? = null,
    val email: String? = null,
    val status: String = "active",
    @SerialName("company_id") val companyId: String
)


@Serializable
data class StaffMemberDto(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("doc_type") val docType: String? = null,
    @SerialName("doc_number") val docNumber: String? = null,
    val role: String,
    val phone: String? = null,
    val email: String? = null,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("company_id") val companyId: String? = null
)

@Serializable
data class ServiceCategoryDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val status: String? = null,
    @SerialName("company_id") val companyId: String? = null
)

@Serializable
data class ServiceDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("category_id") val categoryId: String,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("company_id") val companyId: String? = null,
    val color: String? = null,
    val icon: String? = null,
    @SerialName("service_categories") val serviceCategory: ServiceCategoryDto? = null
)

@Serializable
data class ServiceRefDto(
    val color: String? = null,
    val icon: String? = null
)

@Serializable
data class ServicePricingDto(
    val id: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("vehicle_type_id") val vehicleTypeId: String,
    val price: Double,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("company_id") val companyId: String? = null
)

@Serializable
data class VehicleDto(
    val id: String? = null,   // null on insert → DB generates UUID
    val plate: String,
    val color: String,
    val brand: String,
    val model: String? = null,
    @SerialName("vehicle_type_id") val vehicleTypeId: String,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("company_id") val companyId: String? = null
)

@Serializable
data class VehicleOwnerDto(
    @SerialName("vehicle_id") val vehicleId: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class InventoryItemDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val unit: String,
    val quantity: Double,
    @SerialName("min_quantity") val minQuantity: Double,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("company_id") val companyId: String? = null
)

@Serializable
data class VehicleInsertDto(
    val plate: String,
    val color: String,
    val brand: String,
    val model: String? = null,
    @SerialName("vehicle_type_id") val vehicleTypeId: String,
    val status: String = "active",
    @SerialName("company_id") val companyId: String
)

@Serializable
data class PromotionDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("discount_type") val discountType: String,
    @SerialName("discount_value") val discountValue: Double,
    val scope: String,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("company_id") val companyId: String? = null
)

@Serializable
data class PromotionScopeDto(
    val id: String,
    @SerialName("promotion_id") val promotionId: String,
    @SerialName("scope_ref_id") val scopeRefId: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class PaymentMethodDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("company_id") val companyId: String? = null
)

@Serializable
data class UserProfileDto(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String? = null,
    val role: String,
    @SerialName("company_id") val companyId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)