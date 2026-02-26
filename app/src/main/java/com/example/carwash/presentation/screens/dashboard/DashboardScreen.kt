package com.example.carwash.presentation.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carwash.presentation.components.OrderCard
import com.example.carwash.presentation.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(onAddOrder: () -> Unit, viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = onAddOrder) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar orden")
                }
            }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                            text = "Bienvenido, Usuario",
                            style = MaterialTheme.typography.headlineMedium
                    )
                }

                item {
                    Column {
                        Text(
                                text = "Pendientes y en Progreso",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (uiState.pendingInProgressOrders.isEmpty()) {
                            Text(text = "No hay órdenes pendientes o en progreso.")
                        } else {
                            uiState.pendingInProgressOrders.forEach { order ->
                                OrderCard(order = order)
                            }
                        }
                    }
                }

                item {
                    Column {
                        Text(
                                text = "Entregadas",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (uiState.deliveredOrders.isEmpty()) {
                            Text(text = "No hay órdenes entregadas hoy.")
                        } else {
                            uiState.deliveredOrders.forEach { order -> OrderCard(order = order) }
                        }
                    }
                }
            }
        }
    }
}
