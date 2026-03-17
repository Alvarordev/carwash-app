package com.example.carwash.presentation.navigation

import android.app.Activity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.carwash.R
import com.example.carwash.presentation.components.DateFilterDialog
import com.example.carwash.presentation.screens.dashboard.DashboardScreen
import com.example.carwash.presentation.screens.orders.OrderDetailsScreen
import com.example.carwash.presentation.screens.orders.OrdersScreen
import com.example.carwash.presentation.screens.settings.SettingsScreen
import com.example.carwash.presentation.viewmodel.DashboardStatusFilter
import com.example.carwash.presentation.viewmodel.DashboardViewModel
import com.example.carwash.presentation.viewmodel.DateFilterMode
import com.example.carwash.ui.theme.OrangePrimary

private data class MainScaffoldConfig(
    val title: String,
    val showTopBar: Boolean,
    val showBottomBar: Boolean,
    val showFab: Boolean,
    val topBarColor: Color,
    val topBarContentColor: Color,
)

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Iniciar Sesion")
    object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Home)
    object Orders : Screen("orders", "Ordenes", Icons.Default.List)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
    object AddOrderPhoto : Screen("add_order_photo", "Fotos")
    object AddOrderVehicle : Screen("add_order_vehicle", "Vehiculo")
    object AddOrderCustomer : Screen("add_order_customer", "Cliente")
    object AddOrderServices : Screen("add_order_services", "Servicios")
    object AddOrderObservations : Screen("add_order_observations", "Observaciones")
    object AddOrderSummary : Screen("add_order_summary", "Resumen")
    object OrderDetail : Screen("order_details/{orderId}", "Orden")
}

private val tabRoutes = listOf(Screen.Dashboard.route, Screen.Settings.route)
private const val TAB_ANIM_DURATION = 300

@Composable
fun MainScreen(onAddOrder: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val colorScheme = MaterialTheme.colorScheme
    val scaffoldConfig = currentRoute.toMainScaffoldConfig(colorScheme)

    val navigationItems = listOf(Screen.Dashboard, Screen.Settings)

    MainStatusBarEffect(
        color = scaffoldConfig.topBarColor,
        darkIcons = scaffoldConfig.topBarColor.luminance() > 0.5f
    )

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            if (scaffoldConfig.showTopBar) {
                when (currentRoute) {
                    Screen.Dashboard.route -> {
                        val dashboardViewModel: DashboardViewModel? =
                            navBackStackEntry?.let { hiltViewModel(it) }
                        dashboardViewModel?.let { DashboardTopBar(viewModel = it) }
                    }
                    else -> StandardTopBar(
                        title = scaffoldConfig.title,
                        containerColor = scaffoldConfig.topBarColor,
                        titleColor = scaffoldConfig.topBarContentColor
                    )
                }
            }
        },
        bottomBar = {
            if (scaffoldConfig.showBottomBar) {
                NavigationBar(containerColor = colorScheme.surface) {
                    navigationItems.forEach { screen ->
                        screen.icon?.let { icon ->
                            NavigationBarItem(
                                icon = { Icon(icon, contentDescription = screen.title) },
                                label = { Text(screen.title) },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = OrangePrimary,
                                    selectedTextColor = OrangePrimary,
                                    indicatorColor = colorScheme.surfaceVariant,
                                    unselectedIconColor = colorScheme.onSurfaceVariant,
                                    unselectedTextColor = colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (scaffoldConfig.showFab) {
                FloatingActionButton(
                    onClick = onAddOrder,
                    containerColor = OrangePrimary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva orden")
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                val targetIndex = tabRoutes.indexOf(targetState.destination.route)
                val initialIndex = tabRoutes.indexOf(initialState.destination.route)
                when {
                    targetIndex < 0 || initialIndex < 0 ->
                        slideInHorizontally(tween(TAB_ANIM_DURATION)) { it } + fadeIn(tween(TAB_ANIM_DURATION))
                    targetIndex > initialIndex ->
                        slideInHorizontally(tween(TAB_ANIM_DURATION)) { it }
                    else ->
                        slideInHorizontally(tween(TAB_ANIM_DURATION)) { -it }
                }
            },
            exitTransition = {
                val targetIndex = tabRoutes.indexOf(targetState.destination.route)
                val initialIndex = tabRoutes.indexOf(initialState.destination.route)
                when {
                    targetIndex < 0 || initialIndex < 0 ->
                        slideOutHorizontally(tween(TAB_ANIM_DURATION)) { -it } + fadeOut(tween(TAB_ANIM_DURATION))
                    targetIndex > initialIndex ->
                        slideOutHorizontally(tween(TAB_ANIM_DURATION)) { -it }
                    else ->
                        slideOutHorizontally(tween(TAB_ANIM_DURATION)) { it }
                }
            },
            popEnterTransition = {
                slideInHorizontally(tween(TAB_ANIM_DURATION)) { -it } + fadeIn(tween(TAB_ANIM_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(tween(TAB_ANIM_DURATION)) { it } + fadeOut(tween(TAB_ANIM_DURATION))
            }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onOrderClick = { orderId -> navController.navigate("order_details/$orderId") },
                    onViewAll = {
                        navController.navigate(Screen.Orders.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Orders.route) {
                OrdersScreen(
                    onOrderClick = { orderId -> navController.navigate("order_details/$orderId") }
                )
            }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(
                route = Screen.OrderDetail.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) {
                OrderDetailsScreen(
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
        }
    }
}

// ── Dashboard Top Bar ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    var showFilterDialog by remember { mutableStateOf(false) }
    val isNonDefault = !uiState.isDefaultFilter

    Column(modifier = Modifier.background(colorScheme.surface)) {
        TopAppBar(
            title = {
                Text(
                    text = "Ordenes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            },
            actions = {
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(
                        painter = painterResource(
                            id = if (isNonDefault) R.drawable.calendar_fill
                                 else R.drawable.calendar
                        ),
                        contentDescription = "Filtrar por fecha",
                        tint = if (isNonDefault) OrangePrimary
                               else colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.surface,
                titleContentColor = colorScheme.onSurface
            )
        )

        // Status filter pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashboardStatusFilter.entries.forEach { filter ->
                val isSelected = uiState.statusFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setStatusFilter(filter) },
                    label = {
                        Text(
                            text = filter.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangePrimary,
                        selectedLabelColor = Color.White,
                        containerColor = colorScheme.surfaceVariant,
                        labelColor = colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color.Transparent,
                        selectedBorderColor = Color.Transparent,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }
    }

    if (showFilterDialog) {
        DateFilterDialog(
            currentMode = uiState.dateFilterMode,
            onModeSelected = { mode ->
                viewModel.setDateFilterMode(mode)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

// ── Standard Top Bar ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StandardTopBar(
    title: String,
    containerColor: Color,
    titleColor: Color
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = titleColor
        )
    )
}

// ── Scaffold Config ────────────────────────────────────────────────────

private fun String?.toMainScaffoldConfig(colorScheme: androidx.compose.material3.ColorScheme): MainScaffoldConfig =
    when (this) {
        Screen.Dashboard.route -> MainScaffoldConfig(
            title = "Ordenes",
            showTopBar = true,
            showBottomBar = true,
            showFab = true,
            topBarColor = colorScheme.surface,
            topBarContentColor = colorScheme.onSurface,
        )
        Screen.Orders.route -> MainScaffoldConfig(
            title = "Ordenes",
            showTopBar = true,
            showBottomBar = true,
            showFab = true,
            topBarColor = colorScheme.surface,
            topBarContentColor = colorScheme.onSurface,
        )
        Screen.Settings.route -> MainScaffoldConfig(
            title = "Ajustes",
            showTopBar = true,
            showBottomBar = true,
            showFab = false,
            topBarColor = colorScheme.surface,
            topBarContentColor = colorScheme.onSurface,
        )
        Screen.OrderDetail.route -> MainScaffoldConfig(
            title = "",
            showTopBar = false,
            showBottomBar = false,
            showFab = false,
            topBarColor = colorScheme.background,
            topBarContentColor = colorScheme.onBackground,
        )
        else -> MainScaffoldConfig(
            title = "",
            showTopBar = false,
            showBottomBar = false,
            showFab = false,
            topBarColor = colorScheme.background,
            topBarContentColor = colorScheme.onBackground,
        )
    }

// ── Status Bar ─────────────────────────────────────────────────────────

@Composable
private fun MainStatusBarEffect(color: Color, darkIcons: Boolean) {
    val view = LocalView.current

    SideEffect {
        val activity = view.context as? Activity ?: return@SideEffect
        val window = activity.window
        @Suppress("DEPRECATION")
        window.statusBarColor = color.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons
    }
}
