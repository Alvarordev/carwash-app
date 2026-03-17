package com.example.carwash.presentation.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.carwash.domain.model.OrderPeriod
import com.example.carwash.presentation.components.OrderListCard
import com.example.carwash.presentation.viewmodel.OrdersViewModel
import com.example.carwash.ui.theme.OrangePrimary

@Composable
fun OrdersScreen(
    onOrderClick: (String) -> Unit = {},
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Órdenes",
                color = colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtrar",
                    tint = colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PeriodPill(
                label = "Hoy",
                selected = uiState.selectedPeriod == OrderPeriod.Today,
                onClick = { viewModel.selectPeriod(OrderPeriod.Today) }
            )
            PeriodPill(
                label = "Esta Semana",
                selected = uiState.selectedPeriod == OrderPeriod.ThisWeek,
                onClick = { viewModel.selectPeriod(OrderPeriod.ThisWeek) }
            )
            PeriodPill(
                label = "Este Mes",
                selected = uiState.selectedPeriod == OrderPeriod.ThisMonth,
                onClick = { viewModel.selectPeriod(OrderPeriod.ThisMonth) }
            )
        }

        Spacer(Modifier.height(20.dp))

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            }
            uiState.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.errorMessage ?: "Error",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
            uiState.orders.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay órdenes para este período",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(uiState.orders) { order ->
                        OrderListCard(
                            order = order,
                            modifier = Modifier.clickable { onOrderClick(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) OrangePrimary else colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
