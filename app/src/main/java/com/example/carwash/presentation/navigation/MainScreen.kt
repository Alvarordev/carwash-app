package com.example.carwash.presentation.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.carwash.presentation.screens.addorder.PhotoCaptureScreen
import com.example.carwash.presentation.screens.addorder.ServicesScreen
import com.example.carwash.presentation.screens.addorder.VehicleFormScreen
import com.example.carwash.presentation.screens.dashboard.DashboardScreen
import com.example.carwash.presentation.viewmodel.AddOrderViewModel

const val ADD_ORDER_GRAPH_ROUTE = "add_order_graph"

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Main Screens
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Orders : Screen("orders", "Órdenes", Icons.Default.List)
    object Settings : Screen("settings", "Configuración", Icons.Default.Settings)

    // Add Order Flow
    object AddOrderPhoto : Screen("add_order_photo", "Fotos")
    object AddOrderVehicle : Screen("add_order_vehicle", "Vehículo")
    object AddOrderServices : Screen("add_order_services", "Servicios")
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
        Screen.Dashboard,
        Screen.Orders,
        Screen.Settings
    )

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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(ADD_ORDER_GRAPH_ROUTE) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Order")
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Orders.route) { Text(text = "Orders Screen") } // Placeholder
            composable(Screen.Settings.route) { Text(text = "Settings Screen") } // Placeholder

            addOrderGraph(navController)
        }
    }
}

fun NavGraphBuilder.addOrderGraph(navController: androidx.navigation.NavController) {
    navigation(startDestination = Screen.AddOrderPhoto.route, route = ADD_ORDER_GRAPH_ROUTE) {
        composable(Screen.AddOrderPhoto.route) {
            val viewModel: AddOrderViewModel = hiltViewModel(navController.getBackStackEntry(ADD_ORDER_GRAPH_ROUTE))
            PhotoCaptureScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.AddOrderVehicle.route) {
            val viewModel: AddOrderViewModel = hiltViewModel(navController.getBackStackEntry(ADD_ORDER_GRAPH_ROUTE))
            VehicleFormScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.AddOrderServices.route) {
            val viewModel: AddOrderViewModel = hiltViewModel(navController.getBackStackEntry(ADD_ORDER_GRAPH_ROUTE))
            ServicesScreen(navController = navController, viewModel = viewModel)
        }
    }
}