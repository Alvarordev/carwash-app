package com.example.carwash.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderStatus
import com.example.carwash.domain.model.ServiceCategory
import com.example.carwash.ui.theme.ChipDetallado
import com.example.carwash.ui.theme.ChipEncerado
import com.example.carwash.ui.theme.ChipLavado
import com.example.carwash.ui.theme.OnSurfaceVariantDark
import com.example.carwash.ui.theme.StatusCancelled
import com.example.carwash.ui.theme.StatusDone
import com.example.carwash.ui.theme.StatusInProgress
import com.example.carwash.ui.theme.StatusPending
import com.example.carwash.ui.theme.SurfaceCardDark
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

@Composable
fun ActiveOrderCard(order: Order, modifier: Modifier = Modifier) {
    // Pick the first service name to display as the primary service chip
    val primaryItem = order.items.firstOrNull()
    val serviceName = primaryItem?.serviceName ?: "Servicio"
    val serviceCategory =
            order.items.firstOrNull()?.let {
                // Try to infer category from service name keywords
                when {
                    it.serviceName.contains("Detall", ignoreCase = true) -> ServiceCategory.Detalle
                    it.serviceName.contains("Encera", ignoreCase = true) -> ServiceCategory.Aniadido
                    else -> ServiceCategory.Exterior
                }
            }
    val chipColor =
            when (serviceCategory) {
                ServiceCategory.Detalle -> ChipDetallado
                ServiceCategory.Aniadido -> ChipEncerado
                else -> ChipLavado
            }

    val vehicleDisplayName =
            order.vehicle?.let { v -> "${v.brand} ${v.model ?: ""}".trim() } ?: "Vehículo"
    val plate = order.vehicle?.plate ?: order.orderNumber

    Box(
            modifier =
                    modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCardDark)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Car icon in rounded square
            Box(
                    modifier =
                            Modifier.size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Vehicle info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = vehicleDisplayName,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                )
                Text(
                        text = plate,
                        color = OnSurfaceVariantDark,
                        style = MaterialTheme.typography.bodySmall
                )
            }

            // Service chip + status badge
            Column(horizontalAlignment = Alignment.End) {
                Box(
                        modifier =
                                Modifier.clip(RoundedCornerShape(6.dp))
                                        .background(chipColor.copy(alpha = 0.18f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                            text = serviceName,
                            color = chipColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                val (statusLabel, statusColor) = when (order.status) {
                    OrderStatus.Terminado -> "Terminado" to StatusPending
                    else -> "En Proceso" to StatusInProgress
                }
                Box(
                        modifier =
                                Modifier.clip(RoundedCornerShape(6.dp))
                                        .background(statusColor.copy(alpha = 0.18f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(statusLabel, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// Keep backward-compatible minimal OrderCard for any other usages
@Composable
fun OrderCard(order: Order, modifier: Modifier = Modifier) {
    ActiveOrderCard(order = order, modifier = modifier)
}

@Composable
fun OrderListCard(order: Order, modifier: Modifier = Modifier) {
    val vehicleDisplayName = order.vehicle?.let { v -> "${v.brand} ${v.model ?: ""}".trim() } ?: "Vehículo"
    val plate = order.vehicle?.plate ?: order.orderNumber
    val serviceName = order.items.firstOrNull()?.serviceName ?: "Servicio"
    val timeAgoText = timeAgo(order.createdAt.toString())

    val (statusLabel, statusColor) = when (order.status) {
        OrderStatus.EnProceso  -> "EN PROCESO" to StatusInProgress
        OrderStatus.Terminado  -> "TERMINADO"  to StatusPending
        OrderStatus.Entregado  -> "ENTREGADO"  to StatusDone
        OrderStatus.Cancelado  -> "CANCELADO"  to StatusCancelled
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCardDark)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            // Top row: vehicle name + time ago
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vehicleDisplayName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = timeAgoText,
                    color = OnSurfaceVariantDark,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(4.dp))

            // Plate row
            Text(
                text = plate,
                color = OnSurfaceVariantDark,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(10.dp))

            // Bottom row: service icon + name + status pill
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = OnSurfaceVariantDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = serviceName,
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                // Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private fun timeAgo(createdAt: String?): String {
    if (createdAt == null) return ""
    return try {
        val created = OffsetDateTime.parse(createdAt)
        val now = OffsetDateTime.now()
        val minutes = ChronoUnit.MINUTES.between(created, now)
        when {
            minutes < 60  -> "${minutes}m ago"
            minutes < 1440 -> "${minutes / 60}h ago"
            else           -> "${minutes / 1440}d ago"
        }
    } catch (e: Exception) {
        ""
    }
}
