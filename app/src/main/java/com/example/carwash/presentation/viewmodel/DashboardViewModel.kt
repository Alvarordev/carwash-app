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
import com.example.carwash.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val CACHE_RANGE_DAYS = 30

enum class DashboardStatusFilter(val label: String) {
    Todos("Todos"),
    Pendiente("Pendiente"),
    EnProceso("En proceso"),
    Terminado("Terminado"),
    Entregado("Entregado");
}

sealed class DateFilterMode {
    object Today : DateFilterMode()
    object Week : DateFilterMode()
    object Month : DateFilterMode()
    data class SpecificDate(val date: LocalDate) : DateFilterMode()
}

sealed class OrderSheetType {
    object None : OrderSheetType()
    data class StaffSelection(val order: Order) : OrderSheetType()
    data class QualityChecklist(val order: Order) : OrderSheetType()
    data class Delivery(val order: Order) : OrderSheetType()
}

data class DashboardDaySection(
    val date: LocalDate,
    val orders: List<Order>
)

data class DashboardUiState(
    val allOrders: List<Order> = emptyList(),
    val dateFilterMode: DateFilterMode = DateFilterMode.Week,
    val statusFilter: DashboardStatusFilter = DashboardStatusFilter.Todos,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val sheetType: OrderSheetType = OrderSheetType.None,
    val availableStaff: List<StaffMember> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isSheetSubmitting: Boolean = false
) {
    val isDefaultFilter: Boolean get() = dateFilterMode is DateFilterMode.Week

    val sections: List<DashboardDaySection>
        get() {
            val limaZone = ZoneId.of("America/Lima")
            val filtered = allOrders
                .filter { it.status != OrderStatus.Anulado }
                .filter { order ->
                    when (statusFilter) {
                        DashboardStatusFilter.Todos -> true
                        DashboardStatusFilter.Pendiente -> order.status == OrderStatus.EnProceso
                        DashboardStatusFilter.EnProceso -> order.status == OrderStatus.Lavando
                        DashboardStatusFilter.Terminado -> order.status == OrderStatus.Terminado
                        DashboardStatusFilter.Entregado -> order.status == OrderStatus.Entregado
                    }
                }

            return filtered
                .groupBy { it.createdAt.atZoneSameInstant(limaZone).toLocalDate() }
                .entries
                .sortedByDescending { it.key }
                .map { (date, orders) -> DashboardDaySection(date, orders) }
        }

    val isEmpty: Boolean get() = sections.isEmpty() && !isLoading && errorMessage == null
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
    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())

    val uiState: StateFlow<DashboardUiState> = combine(
        _baseState,
        _allOrders
    ) { base, allOrders ->
        val (startDate, endDate) = dateRange(base.dateFilterMode)
        val filtered = allOrders.filter { order ->
            val orderDate = order.createdAt.atZoneSameInstant(limaZone).toLocalDate()
            !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate)
        }
        base.copy(allOrders = filtered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    init {
        observeCache()

        viewModelScope.launch {
            val startDate = today.minusDays(CACHE_RANGE_DAYS.toLong())
            orderRepository.getOrdersByDateRange(startDate, today)
                .onSuccess { orders ->
                    _allOrders.value = orders
                    _baseState.update { it.copy(isLoading = false, errorMessage = null) }
                }
                .onFailure { err -> applyLoadError(err) }
        }

        viewModelScope.launch {
            orderRepository.observeOrdersByDate(today)
                .catch { err -> applyLoadError(err) }
                .collect { result ->
                    result
                        .onSuccess { _baseState.update { it.copy(isLoading = false, errorMessage = null) } }
                        .onFailure { err -> applyLoadError(err) }
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

    private fun dateRange(mode: DateFilterMode): Pair<LocalDate, LocalDate> = when (mode) {
        is DateFilterMode.Today -> today to today
        is DateFilterMode.Week -> today.minusDays(6) to today
        is DateFilterMode.Month -> today.withDayOfMonth(1) to today
        is DateFilterMode.SpecificDate -> mode.date to mode.date
    }

    private fun observeCache() {
        viewModelScope.launch {
            val startDate = today.minusDays(CACHE_RANGE_DAYS.toLong())
            for (offset in 0..CACHE_RANGE_DAYS) {
                val date = startDate.plusDays(offset.toLong())
                launch {
                    orderRepository.observeCachedOrdersByDate(date).collectLatest { dayOrders ->
                        _allOrders.update { current ->
                            val withoutDay = current.filter { order ->
                                order.createdAt.atZoneSameInstant(limaZone).toLocalDate() != date
                            }
                            (withoutDay + dayOrders).distinctBy { it.id }
                        }
                        if (dayOrders.isNotEmpty()) {
                            _baseState.update { it.copy(isLoading = false, errorMessage = null) }
                        }
                    }
                }
            }
        }
    }

    private fun applyLoadError(error: Throwable) {
        _baseState.update {
            it.copy(
                isLoading = false,
                errorMessage = if (_allOrders.value.isEmpty()) {
                    error.toUserMessage("No pudimos cargar el dashboard. Intenta de nuevo.")
                } else null
            )
        }
    }

    fun setDateFilterMode(mode: DateFilterMode) {
        _baseState.update { it.copy(dateFilterMode = mode) }

        if (mode is DateFilterMode.SpecificDate) {
            val date = mode.date
            val cacheStart = today.minusDays(CACHE_RANGE_DAYS.toLong())
            if (date.isBefore(cacheStart) || date.isAfter(today)) {
                _baseState.update { it.copy(isLoading = true) }
                viewModelScope.launch {
                    orderRepository.getOrdersByDateRange(date, date)
                        .onSuccess { orders ->
                            _allOrders.update { current -> (current + orders).distinctBy { it.id } }
                            _baseState.update { it.copy(isLoading = false, errorMessage = null) }
                        }
                        .onFailure { err -> applyLoadError(err) }
                }
            }
        } else if (mode is DateFilterMode.Month) {
            val monthStart = today.withDayOfMonth(1)
            val cacheStart = today.minusDays(CACHE_RANGE_DAYS.toLong())
            if (monthStart.isBefore(cacheStart)) {
                _baseState.update { it.copy(isLoading = true) }
                viewModelScope.launch {
                    orderRepository.getOrdersByDateRange(monthStart, cacheStart.minusDays(1))
                        .onSuccess { orders ->
                            _allOrders.update { current -> (current + orders).distinctBy { it.id } }
                            _baseState.update { it.copy(isLoading = false, errorMessage = null) }
                        }
                        .onFailure { err -> applyLoadError(err) }
                }
            }
        }
    }

    fun setStatusFilter(filter: DashboardStatusFilter) {
        _baseState.update { it.copy(statusFilter = filter) }
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
