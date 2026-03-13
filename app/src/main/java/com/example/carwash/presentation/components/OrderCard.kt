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
import com.example.carwash.ui.theme.OnSurfaceVariantDark
import com.example.carwash.ui.theme.OrangePrimary
import com.example.carwash.ui.theme.StatusCancelled
import com.example.carwash.ui.theme.StatusDone
import com.example.carwash.ui.theme.StatusInProgress
import com.example.carwash.ui.theme.StatusPending
import com.example.carwash.ui.theme.StatusWashing
import com.example.carwash.ui.theme.SurfaceCardDark
import androidx.core.graphics.toColorInt

@Composable
fun OrderListCard(
    order: Order,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {}
) {
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
    } ?: OnSurfaceVariantDark
    val serviceIconRes = serviceIconDrawable(firstItem?.serviceIcon)

    val (statusLabel, statusColor) = when (order.status) {
        OrderStatus.EnProceso  -> "Pendiente"  to StatusInProgress
        OrderStatus.Lavando    -> "En Proceso" to StatusWashing
        OrderStatus.Terminado  -> "Terminado"  to StatusPending
        OrderStatus.Entregado  -> "Entregado"  to StatusDone
        OrderStatus.Anulado    -> "Anulado"    to StatusCancelled
    }

    val (buttonText, buttonColor) = when (order.status) {
        OrderStatus.EnProceso  -> "Iniciar Lavado"  to StatusInProgress
        OrderStatus.Lavando    -> "Terminar Lavado"  to StatusWashing
        OrderStatus.Terminado  -> "Finalizar Servicio" to StatusPending
        OrderStatus.Entregado  -> "Ver Detalles"     to StatusDone
        OrderStatus.Anulado    -> ""                  to Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(SurfaceCardDark)
            .border(width = 1.dp, color = Color(0xFF414141), shape = RoundedCornerShape(28.dp))
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = vehicleDisplayName,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = plate,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(color = MaterialTheme.colorScheme.background)
                        .border(
                            width = 1.dp,
                            color = Color(0xFF414141),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (serviceIconRes != null) {
                        Icon(
                            painter = painterResource(id = serviceIconRes),
                            contentDescription = null,
                            tint = serviceColor,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = serviceColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = serviceDisplay,
                        color = serviceColor,
                        fontSize = 14.sp,
                    )
                }

                Text(
                    text = "S/ ${total.toInt()}",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (order.status != OrderStatus.Anulado) {
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(
                        text = buttonText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
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
