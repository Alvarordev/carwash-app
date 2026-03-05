package com.example.carwash.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carwash.data.remote.dto.OrderStatus as DtoOrderStatus
import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderStaff
import com.example.carwash.domain.model.OrderStatus
import com.example.carwash.domain.model.StaffMember
import com.example.carwash.domain.repository.OrderRepository
import com.example.carwash.domain.repository.StaffRepository
import com.example.carwash.domain.usecase.GetOrderByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderDetailsUiState(
    val order: Order? = null,
    val availableStaff: List<StaffMember> = emptyList(),
    val selectedStatus: OrderStatus? = null,
    val pendingStaff: List<OrderStaff> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getOrderById: GetOrderByIdUseCase,
    private val orderRepository: OrderRepository,
    private val staffRepository: StaffRepository
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(OrderDetailsUiState())
    val uiState: StateFlow<OrderDetailsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val orderDeferred = async { getOrderById(orderId) }
            val staffDeferred = async { staffRepository.getActiveStaff() }

            val orderResult = orderDeferred.await()
            val staffResult = staffDeferred.await()

            val order = orderResult.getOrNull()
            val staff = staffResult.getOrNull() ?: emptyList()

            _uiState.update {
                it.copy(
                    order = order,
                    availableStaff = staff,
                    pendingStaff = order?.staff ?: emptyList(),
                    selectedStatus = null,
                    isLoading = false,
                    errorMessage = orderResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun selectStatus(status: OrderStatus) {
        val order = _uiState.value.order ?: return
        val nextValid = nextValidStatus(order.status) ?: return
        if (status != nextValid) return
        // Toggle: clicking the already-selected next status deselects it
        val newValue = if (_uiState.value.selectedStatus == status) null else status
        _uiState.update { it.copy(selectedStatus = newValue) }
    }

    fun nextValidStatus(current: OrderStatus): OrderStatus? = when (current) {
        OrderStatus.EnProceso -> OrderStatus.Terminado
        OrderStatus.Terminado -> OrderStatus.Entregado
        else -> null
    }

    fun addStaff(member: StaffMember) {
        val current = _uiState.value.pendingStaff
        if (current.any { it.staffId == member.id }) return
        val newEntry = OrderStaff(
            id = "pending-${member.id}",
            orderId = orderId,
            staffId = member.id,
            staffName = member.fullName,
            roleSnapshot = member.role,
            createdAt = OffsetDateTime.now()
        )
        _uiState.update { it.copy(pendingStaff = current + newEntry) }
    }

    fun removeStaff(orderStaffId: String) {
        _uiState.update { state ->
            state.copy(pendingStaff = state.pendingStaff.filter { it.id != orderStaffId })
        }
    }

    fun saveChanges() {
        val state = _uiState.value
        val order = state.order ?: return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val newStatus = state.selectedStatus
                if (newStatus != null && newStatus != order.status) {
                    val dtoStatus = when (newStatus) {
                        OrderStatus.EnProceso -> DtoOrderStatus.En_Proceso
                        OrderStatus.Terminado -> DtoOrderStatus.Terminado
                        OrderStatus.Cancelado -> DtoOrderStatus.Cancelado
                        OrderStatus.Entregado -> DtoOrderStatus.Entregado
                    }
                    orderRepository.updateOrderStatus(orderId, dtoStatus).getOrThrow()
                }

                val originalStaffIds = order.staff.mapNotNull { it.staffId }.toSet()
                val pendingStaffIds = state.pendingStaff.mapNotNull { it.staffId }.toSet()
                val toAdd = state.pendingStaff
                    .filter { it.staffId != null && it.staffId !in originalStaffIds }
                    .mapNotNull { it.staffId }
                val toRemove = order.staff
                    .filter { it.staffId != null && it.staffId !in pendingStaffIds }
                    .map { it.id }

                if (toAdd.isNotEmpty() || toRemove.isNotEmpty()) {
                    orderRepository.updateOrderStaff(orderId, toAdd, toRemove).getOrThrow()
                }

                getOrderById(orderId)
                    .onSuccess { refreshed ->
                        _uiState.update {
                            it.copy(
                                order = refreshed,
                                pendingStaff = refreshed.staff,
                                selectedStatus = null,
                                isSaving = false,
                                saveSuccess = true
                            )
                        }
                    }
                    .onFailure {
                        _uiState.update { s -> s.copy(isSaving = false, saveSuccess = true) }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.message ?: "Error saving changes")
                }
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
