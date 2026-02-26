package com.example.carwash.domain.repository

import com.example.carwash.domain.model.EntityStatus
import com.example.carwash.domain.model.StaffMember
import com.example.carwash.domain.model.StaffRole
import kotlinx.coroutines.flow.Flow

interface StaffRepository {
    fun getStaff(): Flow<List<StaffMember>>
    suspend fun getActiveStaff(): Result<List<StaffMember>>
    suspend fun getStaffByRole(role: StaffRole): Result<List<StaffMember>>
    suspend fun addStaff(staff: StaffMember): Result<StaffMember>
    suspend fun updateStaff(staff: StaffMember): Result<Unit>
    suspend fun setStaffStatus(id: String, status: EntityStatus): Result<Unit>
}