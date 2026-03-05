package com.example.carwash.data.repository

import com.example.carwash.data.mapper.toDomain
import com.example.carwash.data.remote.datasource.CustomerRemoteDataSource
import com.example.carwash.data.remote.dto.CustomerDto
import com.example.carwash.data.session.CompanySession
import com.example.carwash.domain.model.Customer
import com.example.carwash.domain.model.EntityStatus
import com.example.carwash.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val dataSource: CustomerRemoteDataSource,
    private val companySession: CompanySession
) : CustomerRepository {

    override fun getCustomers(): Flow<List<Customer>> = flow {
        emit(dataSource.getAll().map { it.toDomain() })
    }

    override suspend fun getCustomerById(id: String): Result<Customer> = runCatching {
        dataSource.getById(id).toDomain()
    }

    override suspend fun searchCustomers(query: String): Result<List<Customer>> = runCatching {
        dataSource.searchByName(query).map { it.toDomain() }
    }

    override suspend fun addCustomer(customer: Customer): Result<Customer> = runCatching {
        dataSource.insert(customer.toDto()).toDomain()
    }

    override suspend fun updateCustomer(customer: Customer): Result<Unit> = runCatching {
        dataSource.update(customer.id, customer.toDto())
    }

    override suspend fun setCustomerStatus(id: String, status: EntityStatus): Result<Unit> = runCatching {
        dataSource.setStatus(id, if (status == EntityStatus.Active) "active" else "inactive")
    }

    // Domain → DTO para escritura
    private fun Customer.toDto() = CustomerDto(
        id = id,
        firstName = firstName,
        lastName = lastName,
        docType = docType?.name?.lowercase(),
        docNumber = docNumber,
        phone = phone,
        email = email,
        status = if (status == EntityStatus.Active) "active" else "inactive",
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        companyId = companySession.companyId
    )
}
