package com.example.carwash.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carwash.domain.model.CreateOrderRequest
import com.example.carwash.domain.model.OrderItemRequest
import com.example.carwash.domain.model.Promotion
import com.example.carwash.domain.model.Service
import com.example.carwash.domain.model.VehicleType
import com.example.carwash.domain.usecase.AddOrderUseCase
import com.example.carwash.domain.usecase.GetPromotionsUseCase
import com.example.carwash.domain.usecase.GetServicesUseCase
import com.example.carwash.domain.usecase.GetVehicleTypesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddOrderUiState(
    val photos: List<Uri> = emptyList(),
    val plate: String = "",
    val brand: String = "",
    val model: String = "",
    val color: String = "",
    val vehicleType: VehicleType? = null,
    val selectedServices: List<Service> = emptyList(),
    val observations: String = "",
    val selectedPromotion: Promotion? = null,
    val availableVehicleTypes: List<VehicleType> = emptyList(),
    val availableServices: List<Service> = emptyList(),
    val availablePromotions: List<Promotion> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AddOrderViewModel @Inject constructor(
    private val addOrderUseCase: AddOrderUseCase,
    private val getVehicleTypesUseCase: GetVehicleTypesUseCase,
    private val getServicesUseCase: GetServicesUseCase,
    private val getPromotionsUseCase: GetPromotionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddOrderUiState())
    val uiState: StateFlow<AddOrderUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getVehicleTypesUseCase().onEach { types ->
                _uiState.update { it.copy(availableVehicleTypes = types) }
            }.catch { Log.e("AddOrderViewModel", "Error loading vehicle types", it) }.launchIn(viewModelScope)

            getServicesUseCase().onEach { services ->
                _uiState.update { it.copy(availableServices = services) }
            }.catch { Log.e("AddOrderViewModel", "Error loading services", it) }.launchIn(viewModelScope)

            getPromotionsUseCase().onEach { promotions ->
                _uiState.update { it.copy(availablePromotions = promotions) }
            }.catch { Log.e("AddOrderViewModel", "Error loading promotions", it) }.launchIn(viewModelScope)

             _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onPhotoAdded(uri: Uri) {
        if (_uiState.value.photos.size < 4) {
            _uiState.update { it.copy(photos = it.photos + uri) }
        }
    }

    fun onPhotoRemoved(uri: Uri) {
        _uiState.update { it.copy(photos = it.photos - uri) }
    }

    fun onPhotosReordered(newList: List<Uri>) {
        _uiState.update { it.copy(photos = newList) }
    }

    fun onPlateChanged(plate: String) {
        _uiState.update { it.copy(plate = plate) }
    }

    fun onBrandChanged(brand: String) {
        _uiState.update { it.copy(brand = brand) }
    }

    fun onModelChanged(model: String) {
        _uiState.update { it.copy(model = model) }
    }

    fun onColorChanged(color: String) {
        _uiState.update { it.copy(color = color) }
    }

    fun onVehicleTypeSelected(vehicleType: VehicleType) {
        _uiState.update { it.copy(vehicleType = vehicleType) }
    }

    fun onServiceSelected(service: Service) {
        val currentServices = _uiState.value.selectedServices.toMutableList()
        if (currentServices.contains(service)) {
            currentServices.remove(service)
        } else {
            currentServices.add(service)
        }
        _uiState.update { it.copy(selectedServices = currentServices) }
    }

    fun onServicesConfirmed(services: List<Service>) {
        _uiState.update { it.copy(selectedServices = services) }
    }

    fun onObservationsChanged(observations: String) {
        _uiState.update { it.copy(observations = observations) }
    }

    fun onPromotionSelected(promotion: Promotion) {
        _uiState.update { it.copy(selectedPromotion = promotion) }
    }

    fun createOrder() {
        viewModelScope.launch {
            val currentState = _uiState.value

            // TODO: Step 1 - Get or Create Vehicle from plate, brand, etc., to get a vehicleId.
            // val vehicleId = getOrCreateVehicleUseCase(currentState.plate, ...).id
            val vehicleId: String? = null // Placeholder

            // TODO: Step 2 - Fetch prices for each selected service based on the vehicleType.
            val orderItems = currentState.selectedServices.map {
                // val price = getServicePricingUseCase(it.id, currentState.vehicleType?.id).price
                OrderItemRequest(
                    serviceId = it.id,
                    serviceName = it.name,
                    unitPrice = 0.0 // Placeholder price
                )
            }

            if (orderItems.isEmpty()) {
                Log.w("AddOrderViewModel", "Cannot create order with no services selected.")
                return@launch
            }

            val request = CreateOrderRequest(
                vehicleId = vehicleId,
                items = orderItems,
                notes = currentState.observations,
                // TODO: Set customerId and cashierId from user session or selection.
                customerId = null,
                cashierId = null
            )

            addOrderUseCase(request)
                .onSuccess {
                    Log.d("AddOrderViewModel", "Order created successfully: ${it.id}")
                }
                .onFailure {
                    Log.e("AddOrderViewModel", "Error creating order", it)
                }
        }
    }
}