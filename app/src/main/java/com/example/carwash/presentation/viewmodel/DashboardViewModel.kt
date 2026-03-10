package com.example.carwash.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderStatus
import com.example.carwash.domain.usecase.GetTodayOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class DashboardUiState(
        val pendingInProgressOrders: List<Order> = emptyList(),
        val deliveredOrders: List<Order> = emptyList(),
        val isLoading: Boolean = true,
        val errorMessage: String? = null
) {
    val todayCompletedCount: Int
        get() = deliveredOrders.size
    val inProgressCount: Int
        get() = pendingInProgressOrders.count { it.status == OrderStatus.EnProceso }
    val washingCount: Int
        get() = pendingInProgressOrders.count { it.status == OrderStatus.Lavando }
    val pendingCount: Int
        get() = pendingInProgressOrders.count { it.status == OrderStatus.Terminado }
    val totalTodayCount: Int
        get() = todayCompletedCount + pendingInProgressOrders.size
}

@HiltViewModel
class DashboardViewModel
@Inject
constructor(private val getTodayOrdersUseCase: GetTodayOrdersUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        getTodayOrdersUseCase()
            .onEach { result ->
                result
                    .onSuccess { orders ->
                        _uiState.update {
                            it.copy(
                                pendingInProgressOrders = orders.filter { o ->
                                    o.status == OrderStatus.EnProceso || o.status == OrderStatus.Lavando || o.status == OrderStatus.Terminado
                                },
                                deliveredOrders = orders.filter { o ->
                                    o.status == OrderStatus.Entregado
                                },
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    .onFailure { err ->
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = err.message ?: "Error al cargar las órdenes")
                        }
                    }
            }
            .catch { err ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = err.message ?: "Error al cargar las órdenes")
                }
            }
            .launchIn(viewModelScope)
    }
}
