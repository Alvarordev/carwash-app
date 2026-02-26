package com.example.carwash.presentation.screens.addorder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.carwash.presentation.viewmodel.AddOrderViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderSummaryScreen(
        navController: NavController,
        viewModel: AddOrderViewModel,
        onOrderCreated: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.orderCreated) {
        if (uiState.orderCreated) {
            onOrderCreated() // ← navega fuera del grafo
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Resumen de la Orden") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Atrás"
                                )
                            }
                        }
                )
            }
    ) { paddingValues ->
        LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SummaryCard(title = "Fotos (${uiState.photos.size})") {
                    LazyRow(
                            contentPadding = PaddingValues(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(uiState.photos) { index, uri ->
                            Box {
                                AsyncImage(
                                        model = uri,
                                        contentDescription = "Foto ${index + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier =
                                                Modifier.size(90.dp).clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }
                }
            }

            // ---- Vehicle ----
            item {
                SummaryCard(title = "Vehículo") {
                    Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 8.dp)
                    ) {
                        SummaryRow(label = "Placa", value = uiState.plate.ifBlank { "-" })
                        SummaryRow(label = "Marca", value = uiState.brand.ifBlank { "-" })
                        SummaryRow(label = "Modelo", value = uiState.model.ifBlank { "-" })
                        SummaryRow(label = "Color", value = uiState.color.ifBlank { "-" })
                        SummaryRow(label = "Tipo", value = uiState.vehicleType?.name ?: "-")
                    }
                }
            }

            // ---- Services ----
            item {
                SummaryCard(title = "Servicios (${uiState.selectedServices.size})") {
                    Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 8.dp)
                    ) {
                        if (uiState.selectedServices.isEmpty()) {
                            Text(
                                    "Ningún servicio seleccionado",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            uiState.selectedServices.forEach { service ->
                                val price = uiState.resolvedPrices[service.id] ?: 0.0
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(service.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                            "S/ %.2f".format(price),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total", style = MaterialTheme.typography.titleSmall)
                                Text(
                                        "S/ %.2f".format(uiState.total),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // ---- Staff ----
            item {
                SummaryCard(title = "Staff Asignado") {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                                uiState.selectedStaff?.fullName ?: "Sin asignar",
                                style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // ---- Observations ----
            item {
                SummaryCard(title = "Observaciones") {
                    Text(
                            uiState.observations.ifBlank { "Sin observaciones." },
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                    if (uiState.observations.isBlank())
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // ---- Create Order Button ----
            item {
                Button(
                        onClick = { viewModel.createOrder() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isCreatingOrder
                ) {
                    if (uiState.isCreatingOrder) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Text("  Crear Orden")
                    }
                }

                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, content: @Composable () -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
            shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) { Text(title, style = MaterialTheme.typography.titleSmall) }
            content()
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
}
