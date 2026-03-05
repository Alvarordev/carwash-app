package com.example.carwash.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderPeriod
import com.example.carwash.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val selectedPeriod: OrderPeriod = OrderPeriod.Today,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private val selectedPeriodFlow = MutableStateFlow(OrderPeriod.Today)

    init {
        viewModelScope.launch {
            selectedPeriodFlow
                .flatMapLatest { period -> orderRepository.observeOrdersByPeriod(period) }
                .collect { result ->
                    result
                        .onSuccess { orders ->
                            _uiState.update { it.copy(orders = orders, isLoading = false, errorMessage = null) }
                        }
                        .onFailure { err ->
                            _uiState.update { it.copy(isLoading = false, errorMessage = err.message ?: "Error") }
                        }
                }
        }
    }

    fun selectPeriod(period: OrderPeriod) {
        _uiState.update { it.copy(selectedPeriod = period, isLoading = true) }
        selectedPeriodFlow.value = period
    }
}
