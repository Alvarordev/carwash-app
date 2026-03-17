package com.example.carwash.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carwash.R
import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderStatus
import com.example.carwash.ui.theme.BackgroundDark
import com.example.carwash.ui.theme.OrangePrimary
import com.example.carwash.ui.theme.StatusCancelled
import com.example.carwash.ui.theme.StatusDone
import com.example.carwash.ui.theme.StatusDoneBackground
import com.example.carwash.ui.theme.StatusDoneBackgroundLight
import com.example.carwash.ui.theme.StatusInProgress
import com.example.carwash.ui.theme.StatusInProgressBackground
import com.example.carwash.ui.theme.StatusInProgressBackgroundLight
import com.example.carwash.ui.theme.StatusPending
import com.example.carwash.ui.theme.StatusPendingBackground
import com.example.carwash.ui.theme.StatusPendingBackgroundLight
import com.example.carwash.ui.theme.StatusWashing
import com.example.carwash.ui.theme.StatusWashingBackground
import com.example.carwash.ui.theme.StatusWashingBackgroundLight
import androidx.core.graphics.toColorInt
import com.example.carwash.ui.theme.StatusDoneLight
import com.example.carwash.ui.theme.StatusInProgressLight
import com.example.carwash.ui.theme.StatusPendingLight
import com.example.carwash.ui.theme.StatusWashingLight

@Composable
fun OrderListCard(
    order: Order,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = colorScheme.background == BackgroundDark
    val vehicleDisplayName = order.vehicle?.let { v ->
        "${v.brand} ${v.model ?: ""}".trim().uppercase()
    } ?: "VEHÍCULO"
    val plate = order.vehicle?.plate ?: order.orderNumber
    val firstItem = order.items.firstOrNull()
    val serviceName = firstItem?.serviceName ?: "Servicio"
    val extraCount = order.items.size - 1
    val serviceDisplay = if (extraCount > 0) "$serviceName (+$extraCount)" else serviceName
    val total = order.total

    val serviceColor = firstItem?.serviceColor?.let { hex ->
        runCatching { Color(hex.toColorInt()) }.getOrNull()
    } ?: colorScheme.onSurfaceVariant
    val serviceIconRes = serviceIconDrawable(firstItem?.serviceIcon)

    val (statusLabel, statusColor) = when (order.status) {
        OrderStatus.EnProceso  -> "Pendiente"  to if (isDarkTheme) StatusInProgress else StatusInProgressLight
        OrderStatus.Lavando    -> "En Proceso" to if (isDarkTheme) StatusWashing else StatusWashingLight
        OrderStatus.Terminado  -> "Terminado"  to if (isDarkTheme) StatusPending else StatusPendingLight
        OrderStatus.Entregado  -> "Entregado"  to if (isDarkTheme) StatusDone else StatusDoneLight
        OrderStatus.Anulado    -> "Anulado"    to StatusCancelled
    }

    val buttonText = when (order.status) {
        OrderStatus.EnProceso  -> "Iniciar Lavado"
        OrderStatus.Lavando    -> "Terminar Lavado"
        OrderStatus.Terminado  -> "Finalizar Servicio"
        OrderStatus.Entregado  -> "Ver Detalles"
        OrderStatus.Anulado    -> ""
    }

    val statusIcon = when (order.status) {
        OrderStatus.EnProceso  -> R.drawable.circle
        OrderStatus.Lavando    -> R.drawable.half_circle
        OrderStatus.Terminado  -> R.drawable.quarter_circle
        OrderStatus.Entregado  -> R.drawable.check_circle_solid
        OrderStatus.Anulado    -> null
    }

    val statusColorBackground = when (order.status) {
        OrderStatus.EnProceso  -> if (isDarkTheme) StatusInProgressBackground else StatusInProgressBackgroundLight
        OrderStatus.Lavando    -> if (isDarkTheme) StatusWashingBackground else StatusWashingBackgroundLight
        OrderStatus.Terminado  -> if (isDarkTheme) StatusPendingBackground else StatusPendingBackgroundLight
        OrderStatus.Entregado  -> if (isDarkTheme) StatusDoneBackground else StatusDoneBackgroundLight
        OrderStatus.Anulado    -> Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colorScheme.surface)
            .border(width = 1.dp, color = colorScheme.outline.copy(alpha = 0.5f), shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = vehicleDisplayName,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = plate,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(color = statusColorBackground)
                        .padding(start = 10.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    if (statusIcon != null) {
                        Icon(
                            painter = painterResource(id = statusIcon),
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp).padding(bottom = 1.5.dp)
                        )
                    }
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Column {
                    Text(
                        text = "Servicio",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (serviceIconRes != null) {
                            Icon(
                                painter = painterResource(id = serviceIconRes),
                                contentDescription = null,
                                tint = serviceColor,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = serviceColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = serviceDisplay,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp,
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )

                    Text(
                        text = "S/${total.toInt()}",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = colorScheme.outline.copy(alpha = 0.35f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (order.status != OrderStatus.Anulado) {
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(
                        text = buttonText,
                        color = statusColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, modifier: Modifier = Modifier) {
    OrderListCard(order = order, modifier = modifier)
}

fun serviceIconDrawable(icon: String?): Int? = when (icon) {
    "car"         -> R.drawable.car
    "droplet"     -> R.drawable.droplet
    "star"        -> R.drawable.star
    "soap"        -> R.drawable.soap
    "leaf"        -> R.drawable.leaf
    "flash"       -> R.drawable.flash
    "sun"         -> R.drawable.sun_light
    "wind"        -> R.drawable.wind
    "wrench"      -> R.drawable.wrench
    "tools"       -> R.drawable.tools
    "shield"      -> R.drawable.shield
    "flame"       -> R.drawable.fire_flame
    "bright-star" -> R.drawable.bright_star
    else          -> null
}
