package com.example.carwash.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carwash.presentation.components.ActiveOrderCard
import com.example.carwash.presentation.viewmodel.DashboardViewModel
import com.example.carwash.ui.theme.BackgroundDark
import com.example.carwash.ui.theme.OnSurfaceVariantDark
import com.example.carwash.ui.theme.OrangePrimary
import com.example.carwash.ui.theme.StatusInProgress
import com.example.carwash.ui.theme.StatusPending
import com.example.carwash.ui.theme.SurfaceStatsDark

@Composable
fun DashboardScreen(onAddOrder: () -> Unit, viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
            containerColor = BackgroundDark,
            floatingActionButton = {
                FloatingActionButton(
                        onClick = onAddOrder,
                        containerColor = OrangePrimary,
                        contentColor = Color.White,
                        shape = CircleShape
                ) { Icon(Icons.Default.Add, contentDescription = "Agregar orden") }
            }
    ) { _ ->
        if (uiState.isLoading) {
            Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = OrangePrimary) }
        } else {
            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                item { DashboardHeader() }

                // Stats card
                item {
                    StatsCard(
                            completedCount = uiState.todayCompletedCount,
                            inProgressCount = uiState.inProgressCount,
                            pendingCount = uiState.pendingCount
                    )
                }

                // Active orders section header
                item {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = "Órdenes en curso",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                        )
                        TextButton(onClick = {}) {
                            Text(
                                    text = "Ver todo",
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Active order cards
                if (uiState.pendingInProgressOrders.isEmpty()) {
                    item {
                        Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                    text = "No hay órdenes activas por ahora.",
                                    color = OnSurfaceVariantDark,
                                    style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(uiState.pendingInProgressOrders) { order ->
                        ActiveOrderCard(order = order)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader() {
    Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                    modifier = Modifier.size(46.dp).clip(CircleShape).background(OrangePrimary),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = "U",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                    text = "¡Hola, Usuario! 👋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
            )
        }
        // Notification bell
        IconButton(onClick = {}) {
            Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StatsCard(completedCount: Int, inProgressCount: Int, pendingCount: Int) {
    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceStatsDark)
                            .padding(20.dp)
    ) {
        Column {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                            text = "SERVICIOS DE HOY",
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                                text = "$completedCount",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 48.sp,
                                lineHeight = 48.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                                text = "Completados",
                                color = OnSurfaceVariantDark,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                // Trend icon
                Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(OrangePrimary),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status chips row
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusChip(
                        label = "En Proceso",
                        count = inProgressCount,
                        dotColor = StatusInProgress,
                        modifier = Modifier.weight(1f)
                )
                StatusChip(
                        label = "En Espera",
                        count = pendingCount,
                        dotColor = StatusPending,
                        modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, count: Int, dotColor: Color, modifier: Modifier = Modifier) {
    Box(
            modifier =
                    modifier.clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.07f))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = label, color = OnSurfaceVariantDark, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                    text = "$count",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
            )
        }
    }
}
