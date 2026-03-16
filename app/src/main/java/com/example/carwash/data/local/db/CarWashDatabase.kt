package com.example.carwash.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.carwash.data.local.dao.OrderDao
import com.example.carwash.data.local.entity.OrderCustomerEntity
import com.example.carwash.data.local.entity.OrderEntity
import com.example.carwash.data.local.entity.OrderItemEntity
import com.example.carwash.data.local.entity.OrderStaffEntity
import com.example.carwash.data.local.entity.OrderStatusHistoryEntity
import com.example.carwash.data.local.entity.OrderVehicleEntity

@Database(
    entities = [
        OrderEntity::class,
        OrderCustomerEntity::class,
        OrderVehicleEntity::class,
        OrderItemEntity::class,
        OrderStaffEntity::class,
        OrderStatusHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CarWashDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
}
