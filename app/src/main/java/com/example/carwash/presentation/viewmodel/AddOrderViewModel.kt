package com.example.carwash.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carwash.domain.model.CreateOrderRequest
import com.example.carwash.domain.model.EntityStatus
import com.example.carwash.domain.model.OrderItemRequest
import com.example.carwash.domain.model.Promotion
import com.example.carwash.domain.model.Service
import com.example.carwash.domain.model.StaffMember
import com.example.carwash.domain.model.Vehicle
import com.example.carwash.domain.model.VehicleType
import com.example.carwash.domain.repository.VehicleRepository
import com.example.carwash.domain.usecase.AddOrderUseCase
import com.example.carwash.domain.usecase.GetPromotionsUseCase
import com.example.carwash.domain.usecase.GetServicePricingUseCase
import com.example.carwash.domain.usecase.GetServicesUseCase
import com.example.carwash.domain.usecase.GetStaffUseCase
import com.example.carwash.domain.usecase.GetVehicleTypesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val CASHIER_ID = "33333333-0003-0003-0003-000000000002"

data class AddOrderUiState(
        val photos: List<Uri> = emptyList(),
        val plate: String = "",
        val brand: String = "",
        val model: String = "",
        val color: String = "",
        val vehicleType: VehicleType? = null,
        val selectedServices: List<Service> = emptyList(),
        // Per-service resolved prices (serviceId → price)
        val resolvedPrices: Map<String, Double> = emptyMap(),
        val observations: String = "",
        val selectedStaff: StaffMember? = null,
        val selectedPromotion: Promotion? = null,
        val availableVehicleTypes: List<VehicleType> = emptyList(),
        val availableServices: List<Service> = emptyList(),
        val availableStaff: List<StaffMember> = emptyList(),
        val availablePromotions: List<Promotion> = emptyList(),
        val isLoading: Boolean = true,
        val isCreatingOrder: Boolean = false,
        val orderCreated: Boolean = false,
        val error: String? = null
) {
    /** Subtotal computed from resolved prices, falling back to 0 if not found. */
    val subtotal: Double
        get() = selectedServices.sumOf { resolvedPrices[it.id] ?: 0.0 }
    val total: Double
        get() = subtotal // TODO: apply discounts when promotions exist
}

@HiltViewModel
class AddOrderViewModel
@Inject
constructor(
        private val addOrderUseCase: AddOrderUseCase,
        private val getVehicleTypesUseCase: GetVehicleTypesUseCase,
        private val getServicesUseCase: GetServicesUseCase,
        private val getPromotionsUseCase: GetPromotionsUseCase,
        private val getStaffUseCase: GetStaffUseCase,
        private val getServicePricingUseCase: GetServicePricingUseCase,
        private val vehicleRepository: VehicleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddOrderUiState())
    val uiState: StateFlow<AddOrderUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _uiState.update { it.copy(isLoading = true) }

        getVehicleTypesUseCase()
                .onEach { types ->
                    _uiState.update { it.copy(availableVehicleTypes = types, isLoading = false) }
                }
                .catch { Log.e(TAG, "Error loading vehicle types", it) }
                .launchIn(viewModelScope)

        getServicesUseCase()
                .onEach { services -> _uiState.update { it.copy(availableServices = services) } }
                .catch { Log.e(TAG, "Error loading services", it) }
                .launchIn(viewModelScope)

        getPromotionsUseCase()
                .onEach { promotions ->
                    _uiState.update { it.copy(availablePromotions = promotions) }
                }
                .catch { Log.e(TAG, "Error loading promotions", it) }
                .launchIn(viewModelScope)

        getStaffUseCase()
                .onEach { staff -> _uiState.update { it.copy(availableStaff = staff) } }
                .catch { Log.e(TAG, "Error loading staff", it) }
                .launchIn(viewModelScope)
    }

    // ────────────────────────── Photos ──────────────────────────────────────
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

    // ────────────────────────── Vehicle ─────────────────────────────────────
    fun onPlateChanged(plate: String) = _uiState.update { it.copy(plate = plate) }
    fun onBrandChanged(brand: String) = _uiState.update { it.copy(brand = brand) }
    fun onModelChanged(model: String) = _uiState.update { it.copy(model = model) }
    fun onColorChanged(color: String) = _uiState.update { it.copy(color = color) }
    fun onVehicleTypeSelected(vehicleType: VehicleType) {
        _uiState.update { it.copy(vehicleType = vehicleType) }
        // Re-resolve prices whenever vehicle type changes (prices are per vehicleType)
        refreshPrices()
    }

    // ────────────────────────── Services ────────────────────────────────────
    fun onServiceToggled(service: Service) {
        val current = _uiState.value.selectedServices.toMutableList()
        if (current.contains(service)) current.remove(service) else current.add(service)
        _uiState.update { it.copy(selectedServices = current) }
        refreshPrices()
    }

    fun onServicesConfirmed(services: List<Service>) {
        _uiState.update { it.copy(selectedServices = services) }
        refreshPrices()
    }

    fun onServiceRemoved(service: Service) {
        _uiState.update { it.copy(selectedServices = it.selectedServices - service) }
        refreshPrices()
    }

    /**
     * Re-fetch prices for all currently selected services against the selected vehicle type. Runs
     * async — updates [AddOrderUiState.resolvedPrices] when done.
     */
    private fun refreshPrices() {
        val vehicleTypeId = _uiState.value.vehicleType?.id ?: return
        val services = _uiState.value.selectedServices
        if (services.isEmpty()) {
            _uiState.update { it.copy(resolvedPrices = emptyMap()) }
            return
        }
        viewModelScope.launch {
            val prices = mutableMapOf<String, Double>()
            services.forEach { service ->
                val pricing = getServicePricingUseCase(service.id, vehicleTypeId)
                prices[service.id] = pricing?.price ?: 0.0
            }
            _uiState.update { it.copy(resolvedPrices = prices) }
        }
    }

    // ────────────────────────── Observations & Staff ────────────────────────
    fun onObservationsChanged(observations: String) =
            _uiState.update { it.copy(observations = observations) }
    fun onStaffSelected(staff: StaffMember) = _uiState.update { it.copy(selectedStaff = staff) }
    fun onStaffDeselected() = _uiState.update { it.copy(selectedStaff = null) }

    // ────────────────────────── Promotions ──────────────────────────────────
    fun onPromotionSelected(promotion: Promotion) =
            _uiState.update { it.copy(selectedPromotion = promotion) }

    // ────────────────────────── Create Order ────────────────────────────────
    fun createOrder() {
        viewModelScope.launch {
            val state = _uiState.value

            if (state.selectedServices.isEmpty()) {
                _uiState.update { it.copy(error = "Selecciona al menos un servicio.") }
                return@launch
            }
            if (state.plate.isBlank()) {
                _uiState.update { it.copy(error = "Ingresa la placa del vehículo.") }
                return@launch
            }

            _uiState.update { it.copy(isCreatingOrder = true, error = null) }

            val vehicleId = resolveVehicle(state)
            if (vehicleId == null) {
                _uiState.update {
                    it.copy(isCreatingOrder = false, error = "No se pudo registrar el vehículo.")
                }
                return@launch
            }

            val vehicleTypeId = state.vehicleType?.id
            val orderItems =
                    state.selectedServices.map { service ->
                        val price =
                                if (vehicleTypeId != null) {
                                    state.resolvedPrices[service.id]
                                            ?: getServicePricingUseCase(service.id, vehicleTypeId)
                                                    ?.price
                                                    ?: 0.0
                                } else 0.0
                        OrderItemRequest(
                                serviceId = service.id,
                                serviceName = service.name,
                                unitPrice = price,
                                quantity = 1
                        )
                    }

            val staffIds = listOfNotNull(state.selectedStaff?.id)

            val request =
                    CreateOrderRequest(
                            vehicleId = vehicleId,
                            cashierId = CASHIER_ID,
                            customerId = null,
                            items = orderItems,
                            staffIds = staffIds,
                            notes = state.observations.ifBlank { null },
                            photos = state.photos
                    )

            addOrderUseCase(request)
                    .onSuccess {
                        _uiState.update { it.copy(orderCreated = true, isCreatingOrder = false) }
                    }
                    .onFailure { err ->
                        Log.e(TAG, "Error creating order", err)
                        _uiState.update {
                            it.copy(
                                    error = err.message ?: "Error al crear la orden.",
                                    isCreatingOrder = false
                            )
                        }
                    }
        }
    }

    private suspend fun resolveVehicle(state: AddOrderUiState): String? {
        val existing =
                vehicleRepository.getVehicleByPlate(state.plate.trim().uppercase()).getOrNull()
        if (existing != null) {
            Log.d(TAG, "Vehicle already exists: ${existing.id}")
            return existing.id
        }

        val now = OffsetDateTime.now()
        val newVehicle =
                Vehicle(
                        id = "",
                        plate = state.plate.trim().uppercase(),
                        brand = state.brand.trim(),
                        model = state.model.trim(),
                        color = state.color.trim(),
                        vehicleTypeId = state.vehicleType?.id ?: "",
                        status = EntityStatus.Active,
                        createdAt = now,
                        updatedAt = now
                )
        return vehicleRepository
                .addVehicle(vehicle = newVehicle, customerId = null)
                .onFailure { Log.e(TAG, "Error creating vehicle", it) }
                .getOrNull()
                ?.id
    }

    fun resetOrderCreated() = _uiState.update { it.copy(orderCreated = false) }

    companion object {
        private const val TAG = "AddOrderViewModel"
    }
}
