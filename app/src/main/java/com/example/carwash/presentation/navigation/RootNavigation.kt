package com.example.carwash.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.carwash.presentation.screens.addorder.CustomerScreen
import com.example.carwash.presentation.screens.addorder.ObservationsScreen
import com.example.carwash.presentation.screens.addorder.OrderSummaryScreen
import com.example.carwash.presentation.screens.addorder.PhotoCaptureScreen
import com.example.carwash.presentation.screens.addorder.ServicesScreen
import com.example.carwash.presentation.screens.addorder.VehicleFormScreen
import com.example.carwash.presentation.screens.auth.LoginScreen
import com.example.carwash.presentation.viewmodel.AddOrderViewModel
import com.example.carwash.presentation.viewmodel.AuthViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.jan.supabase.auth.status.SessionStatus

const val MAIN_ROUTE = "main"
const val ADD_ORDER_GRAPH_ROUTE = "add_order_graph"
const val AUTH_GRAPH_ROUTE = "auth_graph"

@Composable
fun RootNavigation(authViewModel: AuthViewModel = hiltViewModel()) {
    val rootNavController = rememberNavController()
    val sessionStatus by authViewModel.sessionStatus.collectAsState()

    if (sessionStatus is SessionStatus.Initializing) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startRoute = if (sessionStatus is SessionStatus.Authenticated) {
        MAIN_ROUTE
    } else {
        AUTH_GRAPH_ROUTE
    }

    NavHost(navController = rootNavController, startDestination = startRoute) {
        // Auth Graph
        navigation(route = AUTH_GRAPH_ROUTE, startDestination = Screen.Login.route) {
            composable(Screen.Login.route) {
                LoginScreen(viewModel = authViewModel)
            }
        }

        // Protected Main Graph
        composable(MAIN_ROUTE) {
            MainScreen(onAddOrder = { rootNavController.navigate(ADD_ORDER_GRAPH_ROUTE) })
        }

        navigation(startDestination = Screen.AddOrderPhoto.route, route = ADD_ORDER_GRAPH_ROUTE) {
            addOrderGraph(
                navController = rootNavController,
                onOrderCreated = {
                    rootNavController.navigate(MAIN_ROUTE) {
                        popUpTo(MAIN_ROUTE) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

fun NavGraphBuilder.addOrderGraph(
    navController: androidx.navigation.NavController,
    onOrderCreated: () -> Unit
) {
    composable(Screen.AddOrderPhoto.route) {
        val parentEntry = remember(it) {
            navController.getBackStackEntry(ADD_ORDER_GRAPH_ROUTE)
        }
        val viewModel: AddOrderViewModel = hiltViewModel(parentEntry)
        PhotoCaptureScreen(navController = navController, viewModel = viewModel)
    }
    composable(Screen.AddOrderVehicle.route) {
        val parentEntry = remember(it) {
            navController.getBackStackEntry(ADD_ORDER_GRAPH_ROUTE)
        }
        val viewModel: AddOrderViewModel = hiltViewModel(parentEntry)
        VehicleFormScreen(navController = navController, viewModel = viewModel)
    }
    composable(Screen.AddOrderCustomer.route) {
        val parentEntry = remember(it) {
            navController.getBackStackEntry(ADD_ORDER_GRAPH_ROUTE)
        }
        val viewModel: AddOrderViewModel = hiltViewModel(parentEntry)
        CustomerScreen(navController = navController, viewModel = viewModel)
    }
    composable(Screen.AddOrderServices.route) {
        val parentEntry = remember(it) {
            navController.getBackStackEntry(ADD_ORDER_GRAPH_ROUTE)
        }
        val viewModel: AddOrderViewModel = hiltViewModel(parentEntry)
        ServicesScreen(navController = navController, viewModel = viewModel)
    }
    composable(Screen.AddOrderObservations.route) {
        val parentEntry = remember(it) {
            navController.getBackStackEntry(ADD_ORDER_GRAPH_ROUTE)
        }
        val viewModel: AddOrderViewModel = hiltViewModel(parentEntry)
        ObservationsScreen(navController = navController, viewModel = viewModel)
    }
    composable(Screen.AddOrderSummary.route) {
        val parentEntry = remember(it) {
            navController.getBackStackEntry(ADD_ORDER_GRAPH_ROUTE)
        }
        val viewModel: AddOrderViewModel = hiltViewModel(parentEntry)
        OrderSummaryScreen(navController = navController, viewModel = viewModel, onOrderCreated = onOrderCreated)
    }
}
