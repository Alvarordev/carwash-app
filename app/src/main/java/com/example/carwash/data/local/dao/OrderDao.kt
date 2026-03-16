package com.example.carwash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.carwash.data.local.entity.OrderCustomerEntity
import com.example.carwash.data.local.entity.OrderEntity
import com.example.carwash.data.local.entity.OrderItemEntity
import com.example.carwash.data.local.entity.OrderStaffEntity
import com.example.carwash.data.local.entity.OrderStatusHistoryEntity
import com.example.carwash.data.local.entity.OrderVehicleEntity
import com.example.carwash.data.local.entity.OrderWithRelationsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Transaction
    @Query(
        """
        SELECT * FROM orders
        WHERE companyId = :companyId AND createdAt >= :startIso AND createdAt <= :endIso
        ORDER BY createdAt DESC
        """
    )
    fun observeOrdersByRange(
        companyId: String,
        startIso: String,
        endIso: String
    ): Flow<List<OrderWithRelationsEntity>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun observeOrderById(orderId: String): Flow<OrderWithRelationsEntity?>

    @Transaction
    suspend fun replaceOrdersForRange(
        companyId: String,
        startIso: String,
        endIso: String,
        orders: List<OrderEntity>,
        customers: List<OrderCustomerEntity>,
        vehicles: List<OrderVehicleEntity>,
        items: List<OrderItemEntity>,
        staff: List<OrderStaffEntity>,
        statusHistory: List<OrderStatusHistoryEntity>
    ) {
        deleteOrderGraphsForRange(companyId, startIso, endIso)
        upsertCustomers(customers)
        upsertVehicles(vehicles)
        upsertOrders(orders)
        upsertOrderItems(items)
        upsertOrderStaff(staff)
        upsertOrderStatusHistory(statusHistory)
    }

    @Transaction
    suspend fun upsertOrderGraph(
        order: OrderEntity,
        customer: OrderCustomerEntity?,
        vehicle: OrderVehicleEntity?,
        items: List<OrderItemEntity>,
        staff: List<OrderStaffEntity>,
        statusHistory: List<OrderStatusHistoryEntity>
    ) {
        customer?.let { upsertCustomers(listOf(it)) }
        vehicle?.let { upsertVehicles(listOf(it)) }
        upsertOrders(listOf(order))
        deleteOrderChildren(order.id)
        upsertOrderItems(items)
        upsertOrderStaff(staff)
        upsertOrderStatusHistory(statusHistory)
    }

    @Query(
        "SELECT id FROM orders WHERE companyId = :companyId AND createdAt >= :startIso AND createdAt <= :endIso"
    )
    suspend fun getOrderIdsForRange(companyId: String, startIso: String, endIso: String): List<String>

    @Query("DELETE FROM orders WHERE companyId = :companyId AND createdAt >= :startIso AND createdAt <= :endIso")
    suspend fun deleteOrdersForRange(companyId: String, startIso: String, endIso: String)

    @Query("DELETE FROM order_items WHERE orderId IN (:orderIds)")
    suspend fun deleteOrderItemsForOrders(orderIds: List<String>)

    @Query("DELETE FROM order_staff WHERE orderId IN (:orderIds)")
    suspend fun deleteOrderStaffForOrders(orderIds: List<String>)

    @Query("DELETE FROM order_status_history WHERE orderId IN (:orderIds)")
    suspend fun deleteOrderStatusHistoryForOrders(orderIds: List<String>)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteOrderItemsForOrder(orderId: String)

    @Query("DELETE FROM order_staff WHERE orderId = :orderId")
    suspend fun deleteOrderStaffForOrder(orderId: String)

    @Query("DELETE FROM order_status_history WHERE orderId = :orderId")
    suspend fun deleteOrderStatusHistoryForOrder(orderId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrders(orders: List<OrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomers(customers: List<OrderCustomerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVehicles(vehicles: List<OrderVehicleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrderItems(items: List<OrderItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrderStaff(staff: List<OrderStaffEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrderStatusHistory(history: List<OrderStatusHistoryEntity>)

    @Transaction
    suspend fun deleteOrderGraphsForRange(companyId: String, startIso: String, endIso: String) {
        val orderIds = getOrderIdsForRange(companyId, startIso, endIso)
        if (orderIds.isNotEmpty()) {
            deleteOrderItemsForOrders(orderIds)
            deleteOrderStaffForOrders(orderIds)
            deleteOrderStatusHistoryForOrders(orderIds)
        }
        deleteOrdersForRange(companyId, startIso, endIso)
    }

    @Transaction
    suspend fun deleteOrderChildren(orderId: String) {
        deleteOrderItemsForOrder(orderId)
        deleteOrderStaffForOrder(orderId)
        deleteOrderStatusHistoryForOrder(orderId)
    }
}
