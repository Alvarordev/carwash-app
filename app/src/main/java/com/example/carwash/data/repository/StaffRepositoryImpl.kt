package com.example.carwash.data.repository

import com.example.carwash.data.mapper.toDomain
import com.example.carwash.data.remote.datasource.StaffRemoteDataSource
import com.example.carwash.data.remote.dto.StaffMemberDto
import com.example.carwash.domain.model.EntityStatus
import com.example.carwash.domain.model.StaffMember
import com.example.carwash.domain.model.StaffRole
import com.example.carwash.domain.repository.StaffRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StaffRepositoryImpl @Inject constructor(
    private val dataSource: StaffRemoteDataSource
) : StaffRepository {

    override fun getStaff(): Flow<List<StaffMember>> = flow {
        emit(dataSource.getAll().map { it.toDomain() })
    }

    override suspend fun getActiveStaff(): Result<List<StaffMember>> = runCatching {
        dataSource.getActive().map { it.toDomain() }
    }

    override suspend fun getStaffByRole(role: StaffRole): Result<List<StaffMember>> = runCatching {
        dataSource.getByRole(role.name.lowercase()).map { it.toDomain() }
    }

    override suspend fun addStaff(staff: StaffMember): Result<StaffMember> = runCatching {
        dataSource.insert(staff.toDto()).toDomain()
    }

    override suspend fun updateStaff(staff: StaffMember): Result<Unit> = runCatching {
        dataSource.update(staff.id, staff.toDto())
    }

    override suspend fun setStaffStatus(id: String, status: EntityStatus): Result<Unit> = runCatching {
        dataSource.setStatus(id, if (status == EntityStatus.Active) "active" else "inactive")
    }

    private fun StaffMember.toDto() = StaffMemberDto(
        id = id,
        firstName = firstName,
        lastName = lastName,
        docType = docType?.name?.lowercase(),
        docNumber = docNumber,
        role = role.name.lowercase(),
        phone = phone,
        email = email,
        status = if (status == EntityStatus.Active) "active" else "inactive",
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}