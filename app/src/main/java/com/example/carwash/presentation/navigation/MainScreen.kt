package com.example.carwash.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.carwash.presentation.screens.dashboard.DashboardScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Main Screens (BottomNav)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Orders : Screen("orders", "Órdenes", Icons.Default.List)
    object Settings : Screen("settings", "Configuración", Icons.Default.Settings)

    // Add Order Wizard Steps
    object AddOrderPhoto : Screen("add_order_photo", "Fotos")
    object AddOrderVehicle : Screen("add_order_vehicle", "Vehículo")
    object AddOrderServices : Screen("add_order_services", "Servicios")
    object AddOrderObservations : Screen("add_order_observations", "Observaciones")
    object AddOrderSummary : Screen("add_order_summary", "Resumen")
}

@Composable
fun MainScreen(onAddOrder: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(Screen.Dashboard, Screen.Orders, Screen.Settings)

    Scaffold(
            bottomBar = {
                BottomAppBar {
                    navigationItems.forEach { screen ->
                        screen.icon?.let {
                            NavigationBarItem(
                                    icon = { Icon(it, contentDescription = screen.title) },
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
                                    }
                            )
                        }
                    }
                }
            }
    ) { paddingValues ->
        NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(onAddOrder = onAddOrder) }
            composable(Screen.Orders.route) { Text(text = "Orders Screen") }
            composable(Screen.Settings.route) { Text(text = "Settings Screen") }
        }
    }
}
