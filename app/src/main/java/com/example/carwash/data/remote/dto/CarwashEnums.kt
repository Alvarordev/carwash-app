package com.example.carwash.data.remote.dto

enum class Status {
    active,
    inactive
}

enum class DocumentType {
    dni,
    carnet_extranjeria,
    pasaporte
}

enum class StaffRole {
    admin,
    washer,
    cashier,
    supervisor
}

enum class ServiceCategory {
    exterior,
    interior,
    detalle,
    añadido
}

enum class DiscountType {
    percentage,
    fixed
}

enum class PromotionScopeType {
    all,
    service,
    vehicleType
}

enum class OrderStatus {
    Pendiente,
    En_Proceso,
    Cancelado,
    Entregado
}

enum class PaymentStatus {
    pendiente,
    pagado,
    parcial
}
