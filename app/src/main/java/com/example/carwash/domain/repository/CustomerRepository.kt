package com.example.carwash.domain.repository

import com.example.carwash.domain.model.Customer
import com.example.carwash.domain.model.EntityStatus
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getCustomers(): Flow<List<Customer>>
    suspend fun getCustomerById(id: String): Result<Customer>
    suspend fun searchCustomers(query: String): Result<List<Customer>>
    suspend fun addCustomer(customer: Customer): Result<Customer>
    suspend fun updateCustomer(customer: Customer): Result<Unit>
    suspend fun setCustomerStatus(id: String, status: EntityStatus): Result<Unit>
}