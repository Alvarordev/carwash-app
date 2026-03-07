package com.example.carwash.data.repository

import com.example.carwash.data.mapper.toDomain
import com.example.carwash.data.remote.datasource.CustomerRemoteDataSource
import com.example.carwash.data.remote.dto.CustomerInsertDto
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

    override suspend fun searchCustomerByPhone(phone: String): Result<Customer?> = runCatching {
        dataSource.searchByPhone(phone)?.toDomain()
    }

    override suspend fun addCustomer(customer: Customer): Result<Customer> = runCatching {
        val companyId = companySession.companyId ?: error("Company session not resolved")
        dataSource.insert(customer.toInsertDto(companyId)).toDomain()
    }

    override suspend fun updateCustomer(customer: Customer): Result<Unit> = runCatching {
        // update uses a full DTO — keep existing pattern but only for updates
        dataSource.update(customer.id, com.example.carwash.data.remote.dto.CustomerDto(
            id = customer.id,
            firstName = customer.firstName,
            lastName = customer.lastName,
            docType = customer.docType?.name?.lowercase(),
            docNumber = customer.docNumber,
            phone = customer.phone,
            email = customer.email,
            status = if (customer.status == EntityStatus.Active) "active" else "inactive",
            createdAt = customer.createdAt.toString(),
            updatedAt = customer.updatedAt.toString(),
            companyId = companySession.companyId
        ))
    }

    override suspend fun setCustomerStatus(id: String, status: EntityStatus): Result<Unit> = runCatching {
        dataSource.setStatus(id, if (status == EntityStatus.Active) "active" else "inactive")
    }

    private fun Customer.toInsertDto(companyId: String) = CustomerInsertDto(
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        docType = docType?.name?.lowercase(),
        docNumber = docNumber,
        email = email,
        status = if (status == EntityStatus.Active) "active" else "inactive",
        companyId = companyId
    )
}
