package com.example.carwash.data.remote.datasource

import android.util.Log
import com.example.carwash.data.remote.dto.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderRemoteDataSource @Inject constructor(
    private val client: SupabaseClient
) {
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
                order_items(*, services(color, icon)),
                order_staff(*),
                order_status_history(*)
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

    suspend fun getAllForPeriod(startIso: String, endIso: String): List<OrderWithDetailsDto> =
        client.postgrest["orders"]
            .select(
                Columns.raw(
                    """
                    *,
                    customers(*),
                    vehicles(*),
                    order_items(*, services(color, icon)),
                    order_staff(*),
                    order_status_history(*)
                    """.trimIndent()
                )
            ) {
                filter {
                    gte("created_at", startIso)
                    lte("created_at", endIso)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()

    fun observeOrdersByPeriod(startIso: String, endIso: String): Flow<List<OrderWithDetailsDto>> =
        callbackFlow {
            trySend(getAllForPeriod(startIso, endIso))
            val channel = client.channel("orders-period-${startIso.take(10)}-${System.currentTimeMillis()}")
            val sub = channel
                .postgresChangeFlow<PostgresAction>(schema = "public") { table = "orders" }
                .onEach { trySend(getAllForPeriod(startIso, endIso)) }
                .launchIn(this)
            channel.subscribe()
            awaitClose { sub.cancel(); launch { withContext(NonCancellable) { client.realtime.removeChannel(channel) } } }
        }

    fun observeTodayOrders(): Flow<List<OrderWithDetailsDto>> = callbackFlow {
        trySend(getAll())

        val channel = client.channel("dashboard-orders")
        val sub = channel
            .postgresChangeFlow<PostgresAction>(schema = "public") { table = "orders" }
            .onEach { trySend(getAll()) }
            .launchIn(this)

        channel.subscribe()

        awaitClose {
            sub.cancel()
            launch { withContext(NonCancellable) { client.realtime.removeChannel(channel) } }
        }
    }

    suspend fun getByIdWithDetails(id: String): OrderWithDetailsDto =
        client.postgrest["orders"]
            .select(
                Columns.raw(
                    """
                    *,
                    customers(*),
                    vehicles(*),
                    order_items(*, services(color, icon)),
                    order_staff(*),
                    order_status_history(*)
                    """.trimIndent()
                )
            ) {
                filter { eq("id", id) }
            }
            .decodeSingle()

    suspend fun getByStatus(status: String): List<OrderDto> =
        client.postgrest["orders"]
            .select {
                filter { eq("status", status) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()

    suspend fun getActiveOrders(): List<OrderWithDetailsDto> =
        client.postgrest["orders"]
            .select(
                Columns.raw(
                    """
                    *,
                    customers(id, first_name, last_name, phone),
                    vehicles(id, plate, brand, color),
                    order_items(*, services(color, icon)),
                    order_staff(*)
                    """.trimIndent()
                )
            ) {
                filter {
                    or {
                        eq("status", "En Proceso")
                        eq("status", "Lavando")
                        eq("status", "Terminado")
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

    suspend fun addAttachment(orderId: String, url: String, companyId: String): OrderAttachmentDto =
        client.postgrest["order_attachments"]
            .insert(mapOf("order_id" to orderId, "url" to url, "company_id" to companyId)) { select() }
            .decodeSingle()

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

    suspend fun updatePhotos(orderId: String, photos: List<String>) {
        client.postgrest["orders"]
            .update(mapOf("photos" to photos)) { filter { eq("id", orderId) } }
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
        companyId: String,
        changedBy: String? = null,
        note: String? = null
    ) {
        client.postgrest["order_status_history"]
            .insert(
                buildMap {
                    put("order_id", orderId)
                    put("status", status)
                    put("company_id", companyId)
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