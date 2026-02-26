package com.example.carwash.data.remote.datasource

import android.util.Log
import com.example.carwash.data.remote.dto.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class OrderRemoteDataSource @Inject constructor(
    private val client: SupabaseClient
) {
    /**
     * Obtiene todas las órdenes con datos básicos, ordenadas por fecha descendente.
     */
    suspend fun getAll(): List<OrderWithDetailsDto> {
        val zoneId = ZoneId.of("America/Lima")
        val today = LocalDate.now(zoneId)
        val startOfDay = today.atStartOfDay(zoneId).toOffsetDateTime().toString()
        val endOfDay = today.atTime(23, 59, 59).atZone(zoneId).toOffsetDateTime().toString()

        return client.postgrest["orders"]
            .select(
                Columns.raw(
                    """
                *,
                customers(*),
                vehicles(*),
                order_items(*),
                order_staff(*)
                """.trimIndent()
                )
            ) {
                filter {
                    gte("created_at", startOfDay)
                    lte("created_at", endOfDay)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()
    }

    /**
     * Obtiene una orden con todos sus detalles expandidos:
     * cliente, vehículo, items y staff asignado.
     */
    suspend fun getByIdWithDetails(id: String): OrderWithDetailsDto =
        client.postgrest["orders"]
            .select(
                Columns.raw(
                    """
                    *,
                    customers(*),
                    vehicles(*),
                    order_items(*),
                    order_staff(*)
                    """.trimIndent()
                )
            ) {
                filter { eq("id", id) }
            }
            .decodeSingle()

    /**
     * Obtiene órdenes por estado (Pendiente, En Proceso, Cancelado, Entregado).
     */
    suspend fun getByStatus(status: String): List<OrderDto> =
        client.postgrest["orders"]
            .select {
                filter { eq("status", status) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()

    /**
     * Obtiene las órdenes activas del día (Pendiente / En Proceso).
     */
    suspend fun getActiveOrders(): List<OrderWithDetailsDto> =
        client.postgrest["orders"]
            .select(
                Columns.raw(
                    """
                    *,
                    customers(id, first_name, last_name, phone),
                    vehicles(id, plate, brand, color),
                    order_items(*),
                    order_staff(*)
                    """.trimIndent()
                )
            ) {
                filter {
                    or {
                        eq("status", "Pendiente")
                        eq("status", "En Proceso")
                    }
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()

    suspend fun getByCustomer(customerId: String): List<OrderDto> =
        client.postgrest["orders"]
            .select {
                filter { eq("customer_id", customerId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()

    suspend fun getByVehicle(vehicleId: String): List<OrderDto> =
        client.postgrest["orders"]
            .select {
                filter { eq("vehicle_id", vehicleId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()

    // ── Inserts ───────────────────────────────

    /**
     * Crea la cabecera de la orden y retorna la orden creada con su ID.
     */
    suspend fun createOrder(dto: CreateOrderDto): OrderDto =
        client.postgrest["orders"]
            .insert(dto) { select() }
            .decodeSingle()

    suspend fun addOrderItem(dto: CreateOrderItemDto): OrderItemDto =
        client.postgrest["order_items"]
            .insert(dto) { select() }
            .decodeSingle()

    suspend fun addOrderItems(items: List<CreateOrderItemDto>) {
        client.postgrest["order_items"].insert(items)
    }

    suspend fun assignStaff(dto: CreateOrderStaffDto) {
        client.postgrest["order_staff"].insert(dto)
    }

    suspend fun assignStaffBatch(staff: List<CreateOrderStaffDto>) {
        client.postgrest["order_staff"].insert(staff)
    }

    suspend fun addAttachment(orderId: String, url: String): OrderAttachmentDto =
        client.postgrest["order_attachments"]
            .insert(mapOf("order_id" to orderId, "url" to url)) { select() }
            .decodeSingle()

    /**
     * Actualiza el estado de la orden y registra el cambio en el historial.
     */
    suspend fun updateStatus(id: String, dto: UpdateOrderStatusDto) {
        client.postgrest["orders"]
            .update(dto) { filter { eq("id", id) } }
    }

    suspend fun updateTotals(id: String, subtotal: Double, discounts: Double, total: Double) {
        client.postgrest["orders"]
            .update(
                mapOf(
                    "subtotal" to subtotal,
                    "discounts" to discounts,
                    "total" to total
                )
            ) { filter { eq("id", id) } }
    }

    suspend fun updatePayment(
        id: String,
        paymentStatus: String,
        paymentMethod: String
    ) {
        client.postgrest["orders"]
            .update(
                mapOf(
                    "payment_status" to paymentStatus,
                    "payment_method" to paymentMethod
                )
            ) { filter { eq("id", id) } }
    }

    suspend fun getStatusHistory(orderId: String): List<OrderStatusHistoryDto> =
        client.postgrest["order_status_history"]
            .select {
                filter { eq("order_id", orderId) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList()

    suspend fun addStatusHistory(
        orderId: String,
        status: String,
        changedBy: String? = null,
        note: String? = null
    ) {
        client.postgrest["order_status_history"]
            .insert(
                buildMap {
                    put("order_id", orderId)
                    put("status", status)
                    changedBy?.let { put("changed_by", it) }
                    note?.let { put("note", it) }
                }
            )
    }

    suspend fun getOrderItems(orderId: String): List<OrderItemDto> =
        client.postgrest["order_items"]
            .select { filter { eq("order_id", orderId) } }
            .decodeList()

    suspend fun deleteOrderItem(itemId: String) {
        client.postgrest["order_items"]
            .delete { filter { eq("id", itemId) } }
    }

    suspend fun getOrderStaff(orderId: String): List<OrderStaffDto> =
        client.postgrest["order_staff"]
            .select { filter { eq("order_id", orderId) } }
            .decodeList()

    suspend fun removeStaffFromOrder(id: String) {
        client.postgrest["order_staff"]
            .delete { filter { eq("id", id) } }
    }

    suspend fun getAttachments(orderId: String): List<OrderAttachmentDto> =
        client.postgrest["order_attachments"]
            .select { filter { eq("order_id", orderId) } }
            .decodeList()

    suspend fun deleteAttachment(id: String) {
        client.postgrest["order_attachments"]
            .delete { filter { eq("id", id) } }
    }
}