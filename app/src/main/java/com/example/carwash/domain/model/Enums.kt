package com.example.carwash.domain.model

enum class EntityStatus { Active, Inactive }

enum class DocumentType { Dni, CarnetExtranjeria, Pasaporte }

enum class StaffRole { Admin, Washer, Cashier, Supervisor }

enum class ServiceCategory { Exterior, Interior, Detalle, Aniadido }

enum class DiscountType { Percentage, Fixed }

enum class PromotionScope { All, Service, VehicleType }

enum class OrderStatus { EnProceso, Lavando, Terminado, Entregado, Anulado }

enum class PaymentStatus { Pendiente, Pagado, Parcial }

enum class OrderPeriod { Today, ThisWeek, ThisMonth }