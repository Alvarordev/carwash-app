# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug       # Debug APK → app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease     # Release APK (signed) → app/build/outputs/apk/release/app-release.apk
./gradlew build               # Full build (assemble + test)
./gradlew clean               # Clean build artifacts
./gradlew lint                # Lint checks
./gradlew test                # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests (device/emulator required)
```

Signing credentials and Supabase keys are read from `local.properties` (not committed). Keys: `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `SUPABASE_URL`, `SUPABASE_KEY`.

## Architecture: MVVM + Clean Architecture

Three layers with strict separation:

**Data** → raw Supabase calls, DTOs, mapping
**Domain** → interfaces, domain models, use cases
**Presentation** → ViewModels, Compose screens

```
data/
  remote/dto/         # Kotlinx Serializable DTOs mirroring DB columns
  remote/datasource/  # Raw Supabase Postgrest calls
  repository/         # Implements domain interfaces; maps DTO ↔ domain models
  session/            # CompanySession singleton (post-login context)
  mapper/Mappers.kt   # Extension fns: Dto.toDomain()
domain/
  model/              # Domain models (Models.kt, OrderModels.kt, Enums.kt)
  repository/         # Repository interfaces
  usecase/            # One class per use case
presentation/
  navigation/         # RootNavigation.kt + MainScreen.kt (nav graphs)
  viewmodel/          # One ViewModel per screen/flow
  screens/            # Compose screens grouped by feature
  components/         # Shared Compose components
di/AppModule.kt       # Hilt wiring for all datasources, repositories, session
```

## Multi-Company / CompanySession

Every data operation is scoped to the authenticated staff member's company.

- After login, `AuthRepositoryImpl` queries `staff_members` by email to resolve `companyId`, `staffMemberId`, and `staffName`, which are stored in `CompanySession` (singleton, injected by Hilt).
- All INSERT DTOs include a non-nullable `company_id` field.
- All read DTOs include `company_id: String? = null` — Supabase RLS filters automatically.
- Repositories that do writes (`OrderRepositoryImpl`, `CustomerRepositoryImpl`, `VehicleRepositoryImpl`) inject `CompanySession` and pass `companyId` to every create operation.

## Navigation Structure

```
RootNavigation
├── AUTH_GRAPH → LoginScreen
└── MAIN_ROUTE
    ├── MainScreen (BottomNav: Dashboard / Orders / Profile)
    │   └── OrderDetailsScreen (route param: orderId)
    └── ADD_ORDER_GRAPH (wizard, 6 steps)
        Photo → Vehicle → Customer → Services → Observations → Summary
```

The AddOrder wizard shares a single `AddOrderViewModel` scoped to the parent nav entry, so state persists across all 6 steps.

`RootNavigation` also renders a network status overlay (offline banner) on top of all screens.

## Key Patterns

**Insert DTOs**: Use a separate `*InsertDto` class (no id/timestamps) — see `VehicleInsertDto` vs `VehicleDto`.

**Datasources**: Thin wrappers around `client.postgrest["table"]`. No business logic here.

**Repositories**: Own business logic: inject `CompanySession`, call datasource, map result. Return domain models, never DTOs.

**ServiceCategory**: Not an enum — it's a domain model (`data class ServiceCategory`) backed by the `service_categories` DB table. `ServiceDto` embeds the joined category via `@SerialName("service_categories") val serviceCategory: ServiceCategoryDto?`. Services reference `categoryId: String` (FK), and `category: ServiceCategory?` is the resolved join.

**Dashboard status advancement**: Order status transitions (EnProceso → Lavando → Terminado → Entregado) are driven from `DashboardScreen` via bottom sheets, not `OrderDetailsScreen`. `DashboardViewModel` manages `OrderSheetType` (sealed class: `StaffSelection`, `QualityChecklist`, `Delivery`). `OrderDetailsScreen` only edits staff/items.

**Date navigation on Dashboard**: `DashboardViewModel` exposes `selectDate(LocalDate)` and uses `observeOrdersByDate` (real-time Flow filtered by day in `America/Lima` timezone). The default is today's date.

**Image upload**: Use `ImageCompressor.compress(contentResolver, uri)` (in `util/ImageCompressor.kt`) before uploading to Supabase Storage. It resizes to max 1920px and compresses at 80% quality with EXIF rotation correction.

**WhatsApp tables** (`whatsapp_*`): Webapp-only. No DTOs or models needed on mobile.

**Theme**: Dark-first Material 3. Colors defined in `ui/theme/Color.kt`. Icons use `Icons.Filled` / `Icons.Outlined` from `material-icons-extended`.

## Supabase Backend

Project ID: `mjocggnioqptesmjotpx` (us-east-1)
Mobile tables: `companies`, `customers`, `staff_members`, `vehicles`, `vehicle_types`, `vehicle_owners`, `services`, `service_categories`, `service_pricing`, `promotions`, `promotion_scopes`, `orders`, `order_items`, `order_staff`, `order_status_history`, `order_attachments`, `inventory_items`
