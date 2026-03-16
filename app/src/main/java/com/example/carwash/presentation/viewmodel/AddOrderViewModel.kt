package com.example.carwash.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carwash.domain.model.CreateOrderRequest
import com.example.carwash.domain.model.Customer
import com.example.carwash.domain.model.EntityStatus
import com.example.carwash.domain.model.OrderItemRequest
import com.example.carwash.domain.model.Promotion
import com.example.carwash.domain.model.Service
import com.example.carwash.domain.model.StaffMember
import com.example.carwash.domain.model.Vehicle
import com.example.carwash.domain.model.VehicleType
import com.example.carwash.data.remote.datasource.VehicleAnalysisDatasource
import com.example.carwash.data.session.CompanySession
import com.example.carwash.domain.repository.CustomerRepository
import com.example.carwash.domain.repository.ServiceRepository
import com.example.carwash.domain.repository.VehicleRepository
import com.example.carwash.domain.usecase.AddOrderUseCase
import com.example.carwash.domain.usecase.GetPromotionsUseCase
import com.example.carwash.domain.usecase.GetServicePricingUseCase
import com.example.carwash.domain.usecase.GetServicesUseCase
import com.example.carwash.domain.usecase.GetStaffUseCase
import com.example.carwash.domain.usecase.GetVehicleTypesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
        val pricedServices: List<Service> = emptyList(),
        val availableStaff: List<StaffMember> = emptyList(),
        val availablePromotions: List<Promotion> = emptyList(),
        val isLoading: Boolean = true,
        val isAnalyzingVehicle: Boolean = false,
        val vehicleAnalyzed: Boolean = false,
        val isCreatingOrder: Boolean = false,
        val orderCreated: Boolean = false,
        val error: String? = null,
        // Customer step
        val customerPhone: String = "",
        val customerFirstName: String = "",
        val customerLastName: String = "",
        val foundCustomer: Customer? = null,
        val isSearchingCustomer: Boolean = false,
        val selectedCustomer: Customer? = null,   // existing customer confirmed by user
        val customerSkipped: Boolean = false
) {
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
        private val serviceRepository: ServiceRepository,
        private val vehicleRepository: VehicleRepository,
        private val vehicleAnalysisDataSource: VehicleAnalysisDatasource,
        private val customerRepository: CustomerRepository,
        private val companySession: CompanySession,
) : ViewModel() {

    private var customerSearchJob: Job? = null

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
                .onEach { services ->
                    _uiState.update { it.copy(availableServices = services) }
                    refreshPrices()
                }
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
    }

    fun onServicesConfirmed(services: List<Service>) {
        _uiState.update { it.copy(selectedServices = services) }
    }

    fun onServiceRemoved(service: Service) {
        _uiState.update { it.copy(selectedServices = it.selectedServices - service) }
    }

    /**
     * Bulk-fetch prices for the selected vehicle type, then update [resolvedPrices] and
     * [pricedServices] (only services that have a price for this vehicle type).
     */
    private fun refreshPrices() {
        val vehicleTypeId = _uiState.value.vehicleType?.id ?: return
        viewModelScope.launch {
            val pricingList = serviceRepository.getPricingByVehicleType(vehicleTypeId)
                .getOrNull() ?: emptyList()
            val prices = pricingList.associate { it.serviceId to it.price }
            val pricedServiceIds = prices.keys
            val filtered = _uiState.value.availableServices.filter { it.id in pricedServiceIds }
            _uiState.update { it.copy(resolvedPrices = prices, pricedServices = filtered) }
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

    // ────────────────────────── Customer ────────────────────────────────────
    fun onCustomerPhoneChanged(phone: String) {
        _uiState.update {
            it.copy(
                customerPhone = phone,
                foundCustomer = null,
                selectedCustomer = null,
                customerSkipped = false,
                customerFirstName = "",
                customerLastName = "",
                isSearchingCustomer = false
            )
        }
        customerSearchJob?.cancel()
        if (phone.length >= 7) {
            customerSearchJob = viewModelScope.launch {
                delay(500)
                _uiState.update { it.copy(isSearchingCustomer = true) }
                val found = customerRepository.searchCustomerByPhone(phone).getOrNull()
                _uiState.update {
                    it.copy(
                        foundCustomer = found,
                        isSearchingCustomer = false,
                        customerFirstName = found?.firstName ?: "",
                        customerLastName = found?.lastName ?: ""
                    )
                }
            }
        }
    }

    fun onCustomerFirstNameChanged(name: String) =
            _uiState.update { it.copy(customerFirstName = name) }

    fun onCustomerLastNameChanged(name: String) =
            _uiState.update { it.copy(customerLastName = name) }

    fun confirmFoundCustomer() =
            _uiState.update { it.copy(selectedCustomer = it.foundCustomer) }

    fun onCustomerSkipped() {
        customerSearchJob?.cancel()
        _uiState.update {
            it.copy(
                customerPhone = "",
                customerFirstName = "",
                customerLastName = "",
                foundCustomer = null,
                isSearchingCustomer = false,
                selectedCustomer = null,
                customerSkipped = true
            )
        }
    }

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

            val customerId = resolveCustomer(state, vehicleId)

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
                            cashierId = companySession.staffMemberId,
                            customerId = customerId,
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

    private suspend fun resolveCustomer(state: AddOrderUiState, vehicleId: String): String? {
        if (state.customerSkipped) return null

        val customerId: String? = when {
            state.selectedCustomer != null -> state.selectedCustomer.id
            state.customerPhone.isNotBlank() && state.customerFirstName.isNotBlank() -> {
                val newCustomer = Customer(
                    id = "",
                    firstName = state.customerFirstName.trim(),
                    lastName = state.customerLastName.trim(),
                    phone = state.customerPhone.trim(),
                    status = EntityStatus.Active,
                    createdAt = java.time.OffsetDateTime.now(),
                    updatedAt = java.time.OffsetDateTime.now()
                )
                customerRepository.addCustomer(newCustomer)
                    .onFailure { Log.e(TAG, "Error creating customer", it) }
                    .getOrNull()?.id
            }
            else -> null
        }

        if (customerId != null) {
            vehicleRepository.linkOwnerToVehicle(vehicleId, customerId)
                .onFailure { Log.e(TAG, "Error linking customer to vehicle", it) }
        }

        return customerId
    }

    fun resetOrderCreated() = _uiState.update { it.copy(orderCreated = false) }

    fun analyzeVehicle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingVehicle = true) }
            vehicleAnalysisDataSource.analyze(_uiState.value.photos)
                    .onSuccess { result ->
                        _uiState.update { state ->
                            state.copy(
                                    isAnalyzingVehicle = false,
                                    vehicleAnalyzed = true,
                                    plate = if (!result.plate.isNullOrBlank()) result.plate else state.plate,
                                    brand = if (!result.brand.isNullOrBlank()) result.brand else state.brand,
                                    model = if (!result.model.isNullOrBlank()) result.model else state.model,
                                    color = if (!result.color.isNullOrBlank()) result.color else state.color,
                            )
                        }
                    }
                    .onFailure {
                        _uiState.update { it.copy(isAnalyzingVehicle = false) }
                    }
        }
    }

    fun onVehicleAnalyzedShown() = _uiState.update { it.copy(vehicleAnalyzed = false) }

    companion object {
        private const val TAG = "AddOrderViewModel"
    }
}
