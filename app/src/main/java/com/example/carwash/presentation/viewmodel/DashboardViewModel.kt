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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val pendingCount: Int
        get() = pendingInProgressOrders.count { it.status == OrderStatus.Pendiente }
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
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getTodayOrdersUseCase()
                    .onSuccess { todayOrders ->
                        _uiState.update {
                            it.copy(
                                    pendingInProgressOrders =
                                            todayOrders.filter { order ->
                                                order.status == OrderStatus.EnProceso ||
                                                        order.status == OrderStatus.Pendiente
                                            },
                                    deliveredOrders =
                                            todayOrders.filter { order ->
                                                order.status == OrderStatus.Entregado
                                            },
                                    isLoading = false,
                                    errorMessage = null
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                    isLoading = false,
                                    errorMessage = error.message ?: "Error al cargar las órdenes"
                            )
                        }
                    }
        }
    }
}
