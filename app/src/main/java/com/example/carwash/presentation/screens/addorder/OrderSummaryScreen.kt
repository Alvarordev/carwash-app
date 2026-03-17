package com.example.carwash.presentation.screens.addorder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.carwash.domain.model.Service
import com.example.carwash.presentation.components.serviceIconDrawable
import com.example.carwash.presentation.viewmodel.AddOrderViewModel
import com.example.carwash.ui.theme.OrangePrimary

@Composable
fun OrderSummaryScreen(
    navController: NavController,
    viewModel: AddOrderViewModel,
    onOrderCreated: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(uiState.orderCreated) {
        if (uiState.orderCreated) {
            onOrderCreated()
        }
    }

    Scaffold(containerColor = colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Top bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = colorScheme.onBackground)
                }
                Text(
                    text = "Resumen de la Orden",
                    color = colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                item {
                    SummarySectionHeader(icon = Icons.Default.DirectionsCar, title = "VEHÍCULO")
                }
                item {
                    val rows = buildList {
                        add("Placa" to uiState.plate.ifBlank { "-" })
                        add("Marca" to uiState.brand.ifBlank { "-" })
                        uiState.model.takeIf { it.isNotBlank() }?.let { add("Modelo" to it) }
                        add("Color" to uiState.color.ifBlank { "-" })
                        uiState.vehicleType?.let { add("Tipo" to it.name) }
                    }
                    SummaryDetailCard(rows = rows)
                }

                item {
                    SummarySectionHeader(icon = Icons.Default.Person, title = "CLIENTE")
                }
                item {
                    val customerRows = when {
                        uiState.customerSkipped -> listOf("Cliente" to "Sin cliente")
                        uiState.selectedCustomer != null -> buildList {
                            add("Nombre" to uiState.selectedCustomer!!.fullName)
                            uiState.selectedCustomer!!.phone?.let { add("Teléfono" to it) }
                        }
                        uiState.customerFirstName.isNotBlank() -> buildList {
                            val name = buildString {
                                append(uiState.customerFirstName.trim())
                                if (uiState.customerLastName.isNotBlank()) append(" ${uiState.customerLastName.trim()}")
                            }
                            add("Nombre" to name)
                            if (uiState.customerPhone.isNotBlank()) add("Teléfono" to uiState.customerPhone)
                        }
                        else -> listOf("Cliente" to "Sin cliente")
                    }
                    SummaryDetailCard(rows = customerRows)
                }

                // ── Fotos ─────────────────────────────────────────────────
                if (uiState.photos.isNotEmpty()) {
                    item {
                        SummarySectionHeader(icon = Icons.Default.Image, title = "FOTOS (${uiState.photos.size})")
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 4.dp)
                        ) {
                            itemsIndexed(uiState.photos) { index, uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Foto ${index + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }

                // ── Servicios ─────────────────────────────────────────────
                item {
                    SummarySectionHeader(icon = Icons.Default.Build, title = "SERVICIOS")
                }
                if (uiState.selectedServices.isEmpty()) {
                    item {
                        Text(
                            "Sin servicios seleccionados",
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } else {
                    items(uiState.selectedServices) { service ->
                        SummaryServiceRow(
                            service = service,
                            price = uiState.resolvedPrices[service.id] ?: 0.0
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                "S/ ${"%.2f".format(uiState.total)}",
                                color = OrangePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                // ── Personal ──────────────────────────────────────────────
                item {
                    SummarySectionHeader(icon = Icons.Default.Group, title = "PERSONAL ASIGNADO")
                }
                item {
                    val staffName = uiState.selectedStaff?.fullName ?: "Sin asignar"
                    val staffRole = uiState.selectedStaff?.role?.name
                    SummaryDetailCard(
                        rows = buildList {
                            add("Nombre" to staffName)
                            staffRole?.let { add("Rol" to it) }
                        }
                    )
                }

                // ── Observaciones ─────────────────────────────────────────
                if (uiState.observations.isNotBlank()) {
                    item {
                        SummarySectionHeader(icon = Icons.Default.Notes, title = "OBSERVACIONES")
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colorScheme.surface)
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(uiState.observations, color = colorScheme.onSurface, fontSize = 14.sp)
                        }
                    }
                }

                // ── Error ─────────────────────────────────────────────────
                uiState.error?.let { error ->
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(error, color = Color(0xFFFF6B6B), fontSize = 13.sp, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }

            // ── CTA ───────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Button(
                    onClick = { viewModel.createOrder() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !uiState.isCreatingOrder,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    if (uiState.isCreatingOrder) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Crear Orden", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SummarySectionHeader(icon: ImageVector, title: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(title, color = colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
    }
}

@Composable
private fun SummaryDetailCard(rows: List<Pair<String, String>>) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
    ) {
        rows.forEachIndexed { index, (key, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(key, color = colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Text(value, color = colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
            }
            if (index < rows.size - 1) {
                HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.35f), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun SummaryServiceRow(service: Service, price: Double) {
    val colorScheme = MaterialTheme.colorScheme
    val iconRes = serviceIconDrawable(service.icon)
    val serviceColor = service.color?.let { hex ->
        runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
    } ?: colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = serviceColor, modifier = Modifier.size(16.dp))
        } else {
            Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(service.name, color = serviceColor, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text("S/ ${"%.2f".format(price)}", color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
