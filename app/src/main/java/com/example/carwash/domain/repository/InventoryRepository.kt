package com.example.carwash.domain.repository

import com.example.carwash.domain.model.InventoryItem
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getInventory(): Flow<List<InventoryItem>>
    suspend fun getLowStockItems(): Result<List<InventoryItem>>
    suspend fun updateQuantity(id: String, quantity: Double): Result<Unit>
    suspend fun addItem(item: InventoryItem): Result<InventoryItem>
    suspend fun updateItem(item: InventoryItem): Result<Unit>
}