package com.example.carwash.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carwash.data.remote.dto.OrderStatus as DtoOrderStatus
import com.example.carwash.data.session.CompanySession
import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderStatus
import com.example.carwash.domain.model.PaymentMethod
import com.example.carwash.domain.model.StaffMember
import com.example.carwash.domain.repository.OrderRepository
import com.example.carwash.domain.repository.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class OrderSheetType {
    object None : OrderSheetType()
    data class StaffSelection(val order: Order) : OrderSheetType()
    data class QualityChecklist(val order: Order) : OrderSheetType()
    data class Delivery(val order: Order) : OrderSheetType()
}

data class DashboardUiState(
    val orders: List<Order> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(ZoneId.of("America/Lima")),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val staffName: String = "Usuario",
    val sheetType: OrderSheetType = OrderSheetType.None,
    val availableStaff: List<StaffMember> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isSheetSubmitting: Boolean = false
) {
    val activeOrders: List<Order>
        get() = orders.filter { it.status != OrderStatus.Anulado }

    val completedCount: Int
        get() = orders.count { it.status == OrderStatus.Entregado }

    val averageServiceTimeMinutes: Long?
        get() {
            val times = orders.mapNotNull { order ->
                val lavando = order.statusHistory
                    .firstOrNull { it.status == OrderStatus.Lavando }
                val terminado = order.statusHistory
                    .firstOrNull { it.status == OrderStatus.Terminado }
                    ?: order.statusHistory.firstOrNull { it.status == OrderStatus.Entregado }
                if (lavando != null && terminado != null) {
                    ChronoUnit.MINUTES.between(lavando.createdAt, terminado.createdAt)
                } else null
            }
            return if (times.isNotEmpty()) times.average().toLong() else null
        }

    val isToday: Boolean
        get() = selectedDate == LocalDate.now(ZoneId.of("America/Lima"))
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val staffRepository: StaffRepository,
    private val companySession: CompanySession
) : ViewModel() {

    private val limaZone = ZoneId.of("America/Lima")
    private val today = LocalDate.now(limaZone)

    private val _baseState = MutableStateFlow(DashboardUiState())
    private val _allWeekOrders = MutableStateFlow<List<Order>>(emptyList())

    val uiState: StateFlow<DashboardUiState> = combine(
        _baseState,
        _allWeekOrders
    ) { base, allOrders ->
        val filtered = allOrders.filter { order ->
            order.createdAt.atZoneSameInstant(limaZone).toLocalDate() == base.selectedDate
        }
        base.copy(orders = filtered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    init {
        _baseState.update { it.copy(staffName = companySession.staffName ?: "Usuario") }

        // 1. Fetch all orders for the last 7 days in one call
        viewModelScope.launch {
            val startDate = today.minusDays(6)
            orderRepository.getOrdersByDateRange(startDate, today)
                .onSuccess { orders ->
                    _allWeekOrders.value = orders
                    _baseState.update { it.copy(isLoading = false, errorMessage = null) }
                }
                .onFailure { err ->
                    _baseState.update { it.copy(isLoading = false, errorMessage = err.message ?: "Error") }
                }
        }

        // 2. Realtime subscription for today — merge live updates into the cache
        viewModelScope.launch {
            orderRepository.observeOrdersByDate(today)
                .catch { err ->
                    _baseState.update { it.copy(isLoading = false, errorMessage = err.message ?: "Error") }
                }
                .collect { result ->
                    result
                        .onSuccess { todayOrders ->
                            _allWeekOrders.update { current ->
                                val pastOrders = current.filter { order ->
                                    order.createdAt.atZoneSameInstant(limaZone).toLocalDate() != today
                                }
                                pastOrders + todayOrders
                            }
                            _baseState.update { it.copy(isLoading = false, errorMessage = null) }
                        }
                        .onFailure { err ->
                            _baseState.update { it.copy(isLoading = false, errorMessage = err.message ?: "Error") }
                        }
                }
        }

        viewModelScope.launch {
            staffRepository.getActiveStaff()
                .onSuccess { staff -> _baseState.update { it.copy(availableStaff = staff) } }
        }
        viewModelScope.launch {
            orderRepository.getPaymentMethods()
                .onSuccess { methods -> _baseState.update { it.copy(paymentMethods = methods) } }
        }
    }

    fun selectDate(date: LocalDate) {
        _baseState.update { it.copy(selectedDate = date) }
    }

    fun onCardAction(order: Order) {
        val sheetType = when (order.status) {
            OrderStatus.EnProceso -> OrderSheetType.StaffSelection(order)
            OrderStatus.Lavando -> OrderSheetType.QualityChecklist(order)
            OrderStatus.Terminado -> OrderSheetType.Delivery(order)
            else -> return
        }
        _baseState.update { it.copy(sheetType = sheetType) }
    }

    fun dismissSheet() {
        _baseState.update { it.copy(sheetType = OrderSheetType.None, isSheetSubmitting = false) }
    }

    fun submitStaffAndAdvance(order: Order, selectedStaffIds: List<String>) {
        _baseState.update { it.copy(isSheetSubmitting = true) }
        viewModelScope.launch {
            if (selectedStaffIds.isNotEmpty()) {
                orderRepository.updateOrderStaff(order.id, selectedStaffIds, emptyList())
            }
            orderRepository.updateOrderStatus(
                orderId = order.id,
                status = DtoOrderStatus.Lavando,
                changedBy = companySession.staffMemberId
            )
            dismissSheet()
        }
    }

    fun submitQualityAndAdvance(order: Order) {
        _baseState.update { it.copy(isSheetSubmitting = true) }
        viewModelScope.launch {
            orderRepository.updateOrderStatus(
                orderId = order.id,
                status = DtoOrderStatus.Terminado,
                changedBy = companySession.staffMemberId
            )
            dismissSheet()
        }
    }

    fun submitDelivery(order: Order, paymentMethod: String, photoUris: List<Uri>) {
        _baseState.update { it.copy(isSheetSubmitting = true) }
        viewModelScope.launch {
            orderRepository.deliverOrder(order.id, paymentMethod, photoUris)
            dismissSheet()
        }
    }
}
