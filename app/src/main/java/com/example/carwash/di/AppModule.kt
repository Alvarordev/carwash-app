package com.example.carwash.di

import com.example.carwash.BuildConfig
import com.example.carwash.data.remote.datasource.*
import com.example.carwash.data.repository.*
import com.example.carwash.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Postgrest)
            install(Storage)
        }

    @Provides @Singleton
    fun provideOrderDataSource(client: SupabaseClient) = OrderRemoteDataSource(client)

    @Provides @Singleton
    fun provideCustomerDataSource(client: SupabaseClient) = CustomerRemoteDataSource(client)

    @Provides @Singleton
    fun provideVehicleDataSource(client: SupabaseClient) = VehicleRemoteDataSource(client)

    @Provides @Singleton
    fun provideStaffDataSource(client: SupabaseClient) = StaffRemoteDataSource(client)

    @Provides @Singleton
    fun provideServiceDataSource(client: SupabaseClient) = ServiceRemoteDataSource(client)

    @Provides @Singleton
    fun provideServicePricingDataSource(client: SupabaseClient) = ServicePricingRemoteDataSource(client)

    @Provides @Singleton
    fun provideInventoryDataSource(client: SupabaseClient) = InventoryRemoteDataSource(client)

    @Provides @Singleton
    fun providePromotionDataSource(client: SupabaseClient) = PromotionRemoteDataSource(client)

    @Provides @Singleton
    fun provideVehicleTypeDataSource(client: SupabaseClient) = VehicleTypeRemoteDataSource(client)

    @Provides @Singleton
    fun provideOrderRepository(
        orderDataSource: OrderRemoteDataSource,
        staffDataSource: StaffRemoteDataSource,
        photoDataSource: PhotoRemoteDataSource
    ): OrderRepository = OrderRepositoryImpl(orderDataSource, staffDataSource, photoDataSource)

    @Provides @Singleton
    fun provideCustomerRepository(
        dataSource: CustomerRemoteDataSource
    ): CustomerRepository = CustomerRepositoryImpl(dataSource)

    @Provides @Singleton
    fun provideVehicleRepository(
        dataSource: VehicleRemoteDataSource
    ): VehicleRepository = VehicleRepositoryImpl(dataSource)

    @Provides @Singleton
    fun provideStaffRepository(
        dataSource: StaffRemoteDataSource
    ): StaffRepository = StaffRepositoryImpl(dataSource)

    @Provides @Singleton
    fun provideVehicleTypeRepository(
        dataSource: VehicleTypeRemoteDataSource
    ): VehicleTypeRepository = VehicleTypeRepositoryImpl(dataSource)

    @Provides @Singleton
    fun provideServiceRepository(
        serviceDataSource: ServiceRemoteDataSource,
        pricingDataSource: ServicePricingRemoteDataSource
    ): ServiceRepository = ServiceRepositoryImpl(serviceDataSource, pricingDataSource)

    @Provides @Singleton
    fun providePromotionRepository(
        dataSource: PromotionRemoteDataSource
    ): PromotionRepository = PromotionRepositoryImpl(dataSource)

    @Provides @Singleton
    fun provideInventoryRepository(
        dataSource: InventoryRemoteDataSource
    ): InventoryRepository = InventoryRepositoryImpl(dataSource)
}