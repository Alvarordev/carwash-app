package com.example.carwash.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import com.example.carwash.BuildConfig
import com.example.carwash.data.local.dao.OrderDao
import com.example.carwash.data.local.db.CarWashDatabase
import com.example.carwash.data.remote.datasource.*
import com.example.carwash.data.repository.*
import com.example.carwash.data.session.CompanySession
import com.example.carwash.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
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
                install(Auth)
                install(Realtime)
            }

    @Provides
    @Singleton
    fun provideCompanyDataSource(client: SupabaseClient) = CompanyRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideOrderDataSource(client: SupabaseClient) = OrderRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideCustomerDataSource(client: SupabaseClient) = CustomerRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideVehicleDataSource(client: SupabaseClient) = VehicleRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideStaffDataSource(client: SupabaseClient) = StaffRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideServiceDataSource(client: SupabaseClient) = ServiceRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideServicePricingDataSource(client: SupabaseClient) =
            ServicePricingRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideInventoryDataSource(client: SupabaseClient) = InventoryRemoteDataSource(client)

    @Provides
    @Singleton
    fun providePaymentMethodDataSource(client: SupabaseClient) = PaymentMethodRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideUserProfileDataSource(client: SupabaseClient) = UserProfileRemoteDataSource(client)

    @Provides
    @Singleton
    fun providePromotionDataSource(client: SupabaseClient) = PromotionRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideVehicleTypeDataSource(client: SupabaseClient) = VehicleTypeRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CarWashDatabase =
            Room.databaseBuilder(context, CarWashDatabase::class.java, "carwash.db").build()

    @Provides
    @Singleton
    fun provideOrderDao(database: CarWashDatabase): OrderDao = database.orderDao()

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
            context.contentResolver

    @Provides
    @Singleton
    fun provideVehicleAnalysisDatasource(
            client: SupabaseClient,
            contentResolver: ContentResolver
    ) = VehicleAnalysisDatasource(client, contentResolver)

    @Provides
    @Singleton
    fun provideOrderRepository(
            orderDataSource: OrderRemoteDataSource,
            staffDataSource: StaffRemoteDataSource,
            photoDataSource: PhotoRemoteDataSource,
            paymentMethodDataSource: PaymentMethodRemoteDataSource,
            orderDao: OrderDao,
            contentResolver: ContentResolver,
            companySession: CompanySession
    ): OrderRepository =
            OrderRepositoryImpl(orderDataSource, staffDataSource, photoDataSource, paymentMethodDataSource, orderDao, contentResolver, companySession)

    @Provides
    @Singleton
    fun provideCustomerRepository(
            dataSource: CustomerRemoteDataSource,
            companySession: CompanySession
    ): CustomerRepository = CustomerRepositoryImpl(dataSource, companySession)

    @Provides
    @Singleton
    fun provideVehicleRepository(
            dataSource: VehicleRemoteDataSource,
            companySession: CompanySession
    ): VehicleRepository = VehicleRepositoryImpl(dataSource, companySession)

    @Provides
    @Singleton
    fun provideStaffRepository(dataSource: StaffRemoteDataSource): StaffRepository =
            StaffRepositoryImpl(dataSource)

    @Provides
    @Singleton
    fun provideVehicleTypeRepository(
            dataSource: VehicleTypeRemoteDataSource
    ): VehicleTypeRepository = VehicleTypeRepositoryImpl(dataSource)

    @Provides
    @Singleton
    fun provideServiceRepository(
            serviceDataSource: ServiceRemoteDataSource,
            pricingDataSource: ServicePricingRemoteDataSource
    ): ServiceRepository = ServiceRepositoryImpl(serviceDataSource, pricingDataSource)

    @Provides
    @Singleton
    fun providePromotionRepository(dataSource: PromotionRemoteDataSource): PromotionRepository =
            PromotionRepositoryImpl(dataSource)

    @Provides
    @Singleton
    fun provideInventoryRepository(dataSource: InventoryRemoteDataSource): InventoryRepository =
            InventoryRepositoryImpl(dataSource)

    @Provides
    @Singleton
    fun provideAuthRepository(
            client: SupabaseClient,
            companyDataSource: CompanyRemoteDataSource,
            companySession: CompanySession
    ): AuthRepository = AuthRepositoryImpl(client, companyDataSource, companySession)

    @Provides
    @Singleton
    fun provideUserProfileRepository(
            client: SupabaseClient,
            dataSource: UserProfileRemoteDataSource
    ): UserProfileRepository = UserProfileRepositoryImpl(client, dataSource)
}
