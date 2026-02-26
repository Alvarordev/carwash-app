package com.example.carwash.domain.usecase

import com.example.carwash.domain.model.StaffMember
import com.example.carwash.domain.repository.StaffRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetStaffUseCase @Inject constructor(private val repository: StaffRepository) {
    operator fun invoke(): Flow<List<StaffMember>> = repository.getStaff()
}
