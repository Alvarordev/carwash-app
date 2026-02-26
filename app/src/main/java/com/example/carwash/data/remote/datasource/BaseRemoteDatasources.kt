package com.example.carwash.data.remote.datasource

import com.example.carwash.data.remote.dto.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import javax.inject.Inject

class CustomerRemoteDataSource @Inject constructor(
    private val client: SupabaseClient
) {
    suspend fun getAll(): List<CustomerDto> =
        client.postgrest["customers"]
            .select()
            .decodeList()

    suspend fun getById(id: String): CustomerDto =
        client.postgrest["customers"]
            .select { filter { eq("id", id) } }
            .decodeSingle()

    suspend fun searchByName(query: String): List<CustomerDto> =
        client.postgrest["customers"]
            .select {
                filter {
                    or {
                        ilike("first_name", "%$query%")
                        ilike("last_name", "%$query%")
                    }
                }
            }
            .decodeList()

    suspend fun searchByDoc(docNumber: String): CustomerDto? =
        client.postgrest["customers"]
            .select { filter { eq("doc_number", docNumber) } }
            .decodeList<CustomerDto>()
            .firstOrNull()

    suspend fun insert(dto: CustomerDto): CustomerDto =
        client.postgrest["customers"]
            .insert(dto) { select() }
            .decodeSingle()

    suspend fun update(id: String, dto: CustomerDto) {
        client.postgrest["customers"]
            .update(dto) { filter { eq("id", id) } }
    }

    suspend fun setStatus(id: String, status: String) {
        client.postgrest["customers"]
            .update(mapOf("status" to status)) { filter { eq("id", id) } }
    }
}

class VehicleTypeRemoteDataSource @Inject constructor(
    private val client: SupabaseClient
) {
    suspend fun getAll(): List<VehicleTypeDto> =
        client.postgrest["vehicle_types"]
            .select()
            .decodeList()

    suspend fun getActive(): List<VehicleTypeDto> =
        client.postgrest["vehicle_types"]
            .select { filter { eq("status", "active") } }
            .decodeList()

    suspend fun getById(id: String): VehicleTypeDto =
        client.postgrest["vehicle_types"]
            .select { filter { eq("id", id) } }
            .decodeSingle()

    suspend fun insert(dto: VehicleTypeDto): VehicleTypeDto =
        client.postgrest["vehicle_types"]
            .insert(dto) { select() }
            .decodeSingle()

    suspend fun update(id: String, dto: VehicleTypeDto) {
        client.postgrest["vehicle_types"]
            .update(dto) { filter { eq("id", id) } }
    }
}

class VehicleRemoteDataSource @Inject constructor(
    private val client: SupabaseClient
) {
    suspend fun getAll(): List<VehicleDto> =
        client.postgrest["vehicles"]
            .select()
            .decodeList()

    suspend fun getById(id: String): VehicleDto =
        client.postgrest["vehicles"]
            .select { filter { eq("id", id) } }
            .decodeSingle()

    suspend fun getByPlate(plate: String): VehicleDto? =
        client.postgrest["vehicles"]
            .select { filter { ilike("plate", plate) } }
            .decodeList<VehicleDto>()
            .firstOrNull()

    suspend fun getByCustomerId(customerId: String): List<VehicleDto> =
        client.postgrest["vehicle_owners"]
            .select(Columns.raw("vehicles(*)")) {
                filter { eq("customer_id", customerId) }
            }
            .decodeList()

    suspend fun insert(dto: VehicleDto): VehicleDto {
        val body = VehicleInsertDto(
            plate = dto.plate,
            color = dto.color,
            brand = dto.brand,
            model = dto.model,
            vehicleTypeId = dto.vehicleTypeId,
            status = dto.status
        )
        return client.postgrest["vehicles"]
            .insert(body) { select() }
            .decodeSingle()
    }

    suspend fun update(id: String, dto: VehicleDto) {
        client.postgrest["vehicles"]
            .update(dto) { filter { eq("id", id) } }
    }

    suspend fun linkOwner(vehicleId: String, customerId: String) {
        client.postgrest["vehicle_owners"]
            .insert(mapOf("vehicle_id" to vehicleId, "customer_id" to customerId))
    }
}

class StaffRemoteDataSource @Inject constructor(
    private val client: SupabaseClient
) {
    suspend fun getAll(): List<StaffMemberDto> =
        client.postgrest["staff_members"]
            .select()
            .decodeList()

    suspend fun getActive(): List<StaffMemberDto> =
        client.postgrest["staff_members"]
            .select { filter { eq("status", "active") } }
            .decodeList()

    suspend fun getByRole(role: String): List<StaffMemberDto> =
        client.postgrest["staff_members"]
            .select {
                filter {
                    eq("role", role)
                    eq("status", "active")
                }
            }
            .decodeList()

    suspend fun getById(id: String): StaffMemberDto =
        client.postgrest["staff_members"]
            .select { filter { eq("id", id) } }
            .decodeSingle()

    suspend fun insert(dto: StaffMemberDto): StaffMemberDto =
        client.postgrest["staff_members"]
            .insert(dto) { select() }
            .decodeSingle()

    suspend fun update(id: String, dto: StaffMemberDto) {
        client.postgrest["staff_members"]
            .update(dto) { filter { eq("id", id) } }
    }

    suspend fun setStatus(id: String, status: String) {
        client.postgrest["staff_members"]
            .update(mapOf("status" to status)) { filter { eq("id", id) } }
    }
}

class ServiceRemoteDataSource @Inject constructor(
    private val client: SupabaseClient
) {
    suspend fun getAll(): List<ServiceDto> =
        client.postgrest["services"]
            .select()
            .decodeList()

    suspend fun getActive(): List<ServiceDto> =
        client.postgrest["services"]
            .select { filter { eq("status", "active") } }
            .decodeList()

    suspend fun getByCategory(category: String): List<ServiceDto> =
        client.postgrest["services"]
            .select { filter { eq("category", category) } }
            .decodeList()

    suspend fun insert(dto: ServiceDto): ServiceDto =
        client.postgrest["services"]
            .insert(dto) { select() }
            .decodeSingle()

    suspend fun update(id: String, dto: ServiceDto) {
        client.postgrest["services"]
            .update(dto) { filter { eq("id", id) } }
    }
}

class ServicePricingRemoteDataSource @Inject constructor(
    private val client: SupabaseClient
) {
    suspend fun getAll(): List<ServicePricingDto> =
        client.postgrest["service_pricing"]
            .select()
            .decodeList()

    suspend fun getByService(serviceId: String): List<ServicePricingDto> =
        client.postgrest["service_pricing"]
            .select { filter { eq("service_id", serviceId) } }
            .decodeList()

    suspend fun getByVehicleType(vehicleTypeId: String): List<ServicePricingDto> =
        client.postgrest["service_pricing"]
            .select { filter { eq("vehicle_type_id", vehicleTypeId) } }
            .decodeList()

    suspend fun getPrice(serviceId: String, vehicleTypeId: String): ServicePricingDto? =
        client.postgrest["service_pricing"]
            .select {
                filter {
                    eq("service_id", serviceId)
                    eq("vehicle_type_id", vehicleTypeId)
                    eq("status", "active")
                }
            }
            .decodeList<ServicePricingDto>()
            .firstOrNull()

    suspend fun upsert(dto: ServicePricingDto) {
        client.postgrest["service_pricing"].upsert(dto)
    }
}

class InventoryRemoteDataSource @Inject constructor(
    private val client: SupabaseClient
) {
    suspend fun getAll(): List<InventoryItemDto> =
        client.postgrest["inventory_items"]
            .select()
            .decodeList()

    suspend fun getLowStock(): List<InventoryItemDto> =
        client.postgrest["inventory_items"]
            .select {
                filter {
                    // quantity <= min_quantity
                    FilterOperator.LTE.let { op ->
                        filter("quantity", op, "min_quantity")
                    }
                    eq("status", "active")
                }
            }
            .decodeList()

    suspend fun getById(id: String): InventoryItemDto =
        client.postgrest["inventory_items"]
            .select { filter { eq("id", id) } }
            .decodeSingle()

    suspend fun insert(dto: InventoryItemDto): InventoryItemDto =
        client.postgrest["inventory_items"]
            .insert(dto) { select() }
            .decodeSingle()

    suspend fun update(id: String, dto: InventoryItemDto) {
        client.postgrest["inventory_items"]
            .update(dto) { filter { eq("id", id) } }
    }

    suspend fun updateQuantity(id: String, quantity: Double) {
        client.postgrest["inventory_items"]
            .update(mapOf("quantity" to quantity)) { filter { eq("id", id) } }
    }
}

class PromotionRemoteDataSource @Inject constructor(
    private val client: SupabaseClient
) {
    suspend fun getAll(): List<PromotionDto> =
        client.postgrest["promotions"]
            .select()
            .decodeList()

    suspend fun getActive(): List<PromotionDto> =
        client.postgrest["promotions"]
            .select { filter { eq("status", "active") } }
            .decodeList()

    suspend fun getById(id: String): PromotionDto =
        client.postgrest["promotions"]
            .select { filter { eq("id", id) } }
            .decodeSingle()

    suspend fun insert(dto: PromotionDto): PromotionDto =
        client.postgrest["promotions"]
            .insert(dto) { select() }
            .decodeSingle()

    suspend fun update(id: String, dto: PromotionDto) {
        client.postgrest["promotions"]
            .update(dto) { filter { eq("id", id) } }
    }
}