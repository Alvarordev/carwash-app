package com.example.carwash.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carwash.data.remote.dto.OrderStatus as DtoOrderStatus
import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderItem
import com.example.carwash.domain.model.OrderItemRequest
import com.example.carwash.domain.model.OrderStaff
import com.example.carwash.domain.model.OrderStatus
import com.example.carwash.domain.model.PaymentMethod
import com.example.carwash.domain.model.Service
import com.example.carwash.domain.model.StaffMember
import com.example.carwash.domain.repository.OrderRepository
import com.example.carwash.domain.repository.StaffRepository
import com.example.carwash.domain.usecase.GetOrderByIdUseCase
import com.example.carwash.domain.usecase.GetServicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderDetailsUiState(
    val order: Order? = null,
    val availableStaff: List<StaffMember> = emptyList(),
    val availableServices: List<Service> = emptyList(),
    val selectedStatus: OrderStatus? = null,
    val pendingStaff: List<OrderStaff> = emptyList(),
    val pendingItems: List<OrderItem> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    val showDeliverySheet: Boolean = false,
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isDelivering: Boolean = false
)

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getOrderById: GetOrderByIdUseCase,
    private val orderRepository: OrderRepository,
    private val staffRepository: StaffRepository,
    private val getServicesUseCase: GetServicesUseCase
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(OrderDetailsUiState())
    val uiState: StateFlow<OrderDetailsUiState> = _uiState.asStateFlow()

    init {
        load()
        getServicesUseCase()
            .onEach { services -> _uiState.update { it.copy(availableServices = services) } }
            .catch { }
            .launchIn(viewModelScope)
        viewModelScope.launch {
            orderRepository.getPaymentMethods()
                .onSuccess { methods -> _uiState.update { it.copy(paymentMethods = methods) } }
        }
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
                    pendingItems = order?.items ?: emptyList(),
                    selectedStatus = null,
                    isLoading = false,
                    errorMessage = orderResult.exceptionOrNull()?.message
                )
            }
        }
    }

    // ── Status ───────────────────────────────────────────────────────────────

    fun selectStatus(status: OrderStatus) {
        val order = _uiState.value.order ?: return
        val nextValid = nextValidStatus(order.status) ?: return
        if (status != nextValid) return
        if (status == OrderStatus.Entregado) {
            _uiState.update { it.copy(showDeliverySheet = true) }
            return
        }
        val newValue = if (_uiState.value.selectedStatus == status) null else status
        _uiState.update { it.copy(selectedStatus = newValue) }
    }

    fun undoStatus() = _uiState.update { it.copy(selectedStatus = null) }

    fun dismissDeliverySheet() {
        _uiState.update { it.copy(showDeliverySheet = false) }
    }

    fun confirmDelivery(paymentMethod: String, photoUris: List<Uri>) {
        _uiState.update { it.copy(isDelivering = true) }
        viewModelScope.launch {
            orderRepository.deliverOrder(orderId, paymentMethod, photoUris)
                .onSuccess {
                    _uiState.update { it.copy(isDelivering = false, showDeliverySheet = false) }
                    getOrderById(orderId)
                        .onSuccess { refreshed ->
                            _uiState.update {
                                it.copy(
                                    order = refreshed,
                                    pendingStaff = refreshed.staff,
                                    pendingItems = refreshed.items,
                                    selectedStatus = null,
                                    saveSuccess = true
                                )
                            }
                        }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isDelivering = false,
                            errorMessage = e.message ?: "Error al entregar la orden"
                        )
                    }
                }
        }
    }

    fun nextValidStatus(current: OrderStatus): OrderStatus? = when (current) {
        OrderStatus.EnProceso -> OrderStatus.Lavando
        OrderStatus.Lavando -> OrderStatus.Terminado
        OrderStatus.Terminado -> OrderStatus.Entregado
        else -> null
    }

    // ── Staff ────────────────────────────────────────────────────────────────

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

    // ── Services ─────────────────────────────────────────────────────────────

    /** Replace pending items with the given service list, preserving existing items' data. */
    fun setServices(services: List<Service>) {
        val current = _uiState.value.pendingItems
        val kept = current.filter { item -> services.any { it.id == item.serviceId } }
        val keptIds = kept.mapNotNull { it.serviceId }.toSet()
        val added = services
            .filter { it.id !in keptIds }
            .map { service ->
                OrderItem(
                    id = "pending-${service.id}",
                    orderId = orderId,
                    serviceId = service.id,
                    serviceName = service.name,
                    unitPrice = 0.0,
                    quantity = 1,
                    subtotal = 0.0,
                    createdAt = OffsetDateTime.now(),
                    serviceColor = service.color,
                    serviceIcon = service.icon
                )
            }
        _uiState.update { it.copy(pendingItems = kept + added) }
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    fun saveChanges() {
        val state = _uiState.value
        val order = state.order ?: return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // Status
                val newStatus = state.selectedStatus
                if (newStatus != null && newStatus != order.status) {
                    val dtoStatus = when (newStatus) {
                        OrderStatus.EnProceso -> DtoOrderStatus.En_Proceso
                        OrderStatus.Lavando -> DtoOrderStatus.Lavando
                        OrderStatus.Terminado -> DtoOrderStatus.Terminado
                        OrderStatus.Entregado -> DtoOrderStatus.Entregado
                        OrderStatus.Anulado -> DtoOrderStatus.Anulado
                    }
                    orderRepository.updateOrderStatus(orderId, dtoStatus).getOrThrow()
                }

                // Staff
                val originalStaffIds = order.staff.mapNotNull { it.staffId }.toSet()
                val pendingStaffIds = state.pendingStaff.mapNotNull { it.staffId }.toSet()
                val staffToAdd = state.pendingStaff
                    .filter { it.staffId != null && it.staffId !in originalStaffIds }
                    .mapNotNull { it.staffId }
                val staffToRemove = order.staff
                    .filter { it.staffId != null && it.staffId !in pendingStaffIds }
                    .map { it.id }
                if (staffToAdd.isNotEmpty() || staffToRemove.isNotEmpty()) {
                    orderRepository.updateOrderStaff(orderId, staffToAdd, staffToRemove).getOrThrow()
                }

                // Items
                val originalItemIds = order.items.map { it.id }.toSet()
                val pendingItemIds = state.pendingItems.map { it.id }.toSet()
                val itemsToRemove = order.items
                    .filter { it.id !in pendingItemIds }
                    .map { it.id }
                val itemsToAdd = state.pendingItems
                    .filter { it.id.startsWith("pending-") }
                    .map { item ->
                        OrderItemRequest(
                            serviceId = item.serviceId,
                            serviceName = item.serviceName,
                            unitPrice = item.unitPrice,
                            quantity = item.quantity
                        )
                    }
                if (itemsToAdd.isNotEmpty() || itemsToRemove.isNotEmpty()) {
                    orderRepository.updateOrderItems(orderId, itemsToAdd, itemsToRemove).getOrThrow()
                }

                getOrderById(orderId)
                    .onSuccess { refreshed ->
                        _uiState.update {
                            it.copy(
                                order = refreshed,
                                pendingStaff = refreshed.staff,
                                pendingItems = refreshed.items,
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
