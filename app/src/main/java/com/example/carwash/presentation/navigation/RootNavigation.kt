package com.example.carwash.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.carwash.util.NetworkMonitor
import io.github.jan.supabase.auth.status.SessionStatus

const val MAIN_ROUTE = "main"
const val ADD_ORDER_GRAPH_ROUTE = "add_order_graph"
const val AUTH_GRAPH_ROUTE = "auth_graph"

@Composable
fun RootNavigation(
    networkMonitor: NetworkMonitor,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val rootNavController = rememberNavController()
    val sessionStatus by authViewModel.sessionStatus.collectAsState()
    val isOnline by networkMonitor.isOnline.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (sessionStatus is SessionStatus.Initializing) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val startRoute = if (sessionStatus is SessionStatus.Authenticated) {
                MAIN_ROUTE
            } else {
                AUTH_GRAPH_ROUTE
            }

            NavHost(navController = rootNavController, startDestination = startRoute) {
                navigation(route = AUTH_GRAPH_ROUTE, startDestination = Screen.Login.route) {
                    composable(Screen.Login.route) {
                        LoginScreen(viewModel = authViewModel)
                    }
                }

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

        AnimatedVisibility(
            visible = !isOnline,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(300)) { it },
            exit = slideOutVertically(tween(300)) { it }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB71C1C))
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Sin conexión a internet",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
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
