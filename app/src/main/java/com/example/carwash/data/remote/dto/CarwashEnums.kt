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
    En_Proceso,
    Lavando,
    Terminado,
    Entregado,
    Anulado
}

enum class PaymentStatus {
    pendiente,
    pagado,
    parcial
}
