package com.example.carwash.data.repository

import com.example.carwash.data.mapper.toDomain
import com.example.carwash.data.remote.datasource.InventoryRemoteDataSource
import com.example.carwash.data.remote.dto.InventoryItemDto
import com.example.carwash.domain.model.EntityStatus
import com.example.carwash.domain.model.InventoryItem
import com.example.carwash.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
class InventoryRepositoryImpl @Inject constructor(
    private val dataSource: InventoryRemoteDataSource
) : InventoryRepository {

    override fun getInventory(): Flow<List<InventoryItem>> = flow {
        emit(dataSource.getAll().map { it.toDomain() })
    }

    override suspend fun getLowStockItems(): Result<List<InventoryItem>> = runCatching {
        dataSource.getLowStock().map { it.toDomain() }
    }

    override suspend fun updateQuantity(id: String, quantity: Double): Result<Unit> = runCatching {
        dataSource.updateQuantity(id, quantity)
    }

    override suspend fun addItem(item: InventoryItem): Result<InventoryItem> = runCatching {
        dataSource.insert(item.toDto()).toDomain()
    }

    override suspend fun updateItem(item: InventoryItem): Result<Unit> = runCatching {
        dataSource.update(item.id, item.toDto())
    }

    private fun InventoryItem.toDto() = InventoryItemDto(
        id = id,
        name = name,
        description = description,
        unit = unit,
        quantity = quantity,
        minQuantity = minQuantity,
        status = if (status == EntityStatus.Active) "active" else "inactive",
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}