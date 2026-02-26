package com.example.carwash.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.example.carwash.ui.theme.OnSurfaceDark
import com.example.carwash.ui.theme.OrangePrimary
import com.example.carwash.ui.theme.SurfaceDark

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Main Screens (BottomNav)
    object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Home)
    object Orders : Screen("orders", "Órdenes", Icons.Default.List)
    object Inventory : Screen("inventory", "Inventario", Icons.Default.Inventory2)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)

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

    val navigationItems = listOf(Screen.Dashboard, Screen.Orders, Screen.Inventory, Screen.Profile)

    Scaffold(
            bottomBar = {
                NavigationBar(containerColor = SurfaceDark) {
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
                                    colors =
                                            NavigationBarItemDefaults.colors(
                                                    selectedIconColor = OrangePrimary,
                                                    selectedTextColor = OrangePrimary,
                                                    indicatorColor = SurfaceDark,
                                                    unselectedIconColor = OnSurfaceDark,
                                                    unselectedTextColor = OnSurfaceDark
                                            )
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
            composable(Screen.Inventory.route) { Text(text = "Inventario Screen") }
            composable(Screen.Profile.route) { Text(text = "Perfil Screen") }
        }
    }
}
