package com.example.carwash.domain.model

enum class EntityStatus { Active, Inactive }

enum class DocumentType { Dni, CarnetExtranjeria, Pasaporte }

enum class StaffRole { Admin, Washer, Cashier, Supervisor }

data class ServiceCategory(
    val id: String,
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null
)

enum class DiscountType { Percentage, Fixed }

enum class PromotionScope { All, Service, VehicleType }

enum class OrderStatus { EnProceso, Lavando, Terminado, Entregado, Anulado }

enum class PaymentStatus { Pendiente, Pagado, Parcial }

enum class OrderPeriod { Today, ThisWeek, ThisMonth }

enum class UserProfileRole {
    SuperAdmin, Admin, Operator;

    val displayName: String
        get() = when (this) {
            SuperAdmin -> "Super Admin"
            Admin -> "Administrador"
            Operator -> "Operador"
        }
}