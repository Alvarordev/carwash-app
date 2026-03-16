package com.example.carwash.presentation.screens.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carwash.domain.model.OrderStatus
import com.example.carwash.presentation.components.ChecklistBottomSheet
import com.example.carwash.presentation.components.ChecklistItem
import com.example.carwash.presentation.components.DeliveryBottomSheet
import com.example.carwash.presentation.components.OrderListCard
import com.example.carwash.presentation.viewmodel.DashboardViewModel
import com.example.carwash.presentation.viewmodel.OrderSheetType
import com.example.carwash.ui.theme.BackgroundDark
import com.example.carwash.ui.theme.OnSurfaceVariantDark
import com.example.carwash.ui.theme.OrangePrimary
import com.example.carwash.ui.theme.SurfaceDark
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

private const val DATE_RANGE_DAYS = 29

@Composable
fun DashboardScreen(
    onAddOrder: () -> Unit,
    onOrderClick: (String) -> Unit = {},
    onViewAll: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = remember { LocalDate.now(ZoneId.of("America/Lima")) }
    val last30Days = remember { (DATE_RANGE_DAYS downTo 0).map { today.minusDays(it.toLong()) } }

    val dateListState = rememberLazyListState()
    val selectedIndex = remember(uiState.selectedDate) {
        last30Days.indexOf(uiState.selectedDate).coerceAtLeast(0)
    }

    LaunchedEffect(Unit) {
        dateListState.scrollToItem(
            index = (last30Days.size - 1).coerceAtLeast(0),
            scrollOffset = 0
        )
    }

    LaunchedEffect(selectedIndex) {
        dateListState.animateScrollToItem(
            index = (selectedIndex - 1).coerceAtLeast(0)
        )
    }

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
        Column(modifier = Modifier.fillMaxSize()) {
            // Top header section with SurfaceDark background
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(top = 24.dp, bottom = 16.dp)
            ) {
                // Title
                Text(
                    text = "Ordenes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                // Month + year subtitle
                Text(
                    text = "${spanishMonthName(uiState.selectedDate.month)} ${uiState.selectedDate.year}",
                    color = OnSurfaceVariantDark,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Horizontally scrollable date strip
                LazyRow(
                    state = dateListState,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(last30Days, key = { _, date -> date.toEpochDay() }) { _, date ->
                        DateItem(
                            date = date,
                            isSelected = date == uiState.selectedDate,
                            isToday = date == today,
                            onClick = { viewModel.selectDate(date) }
                        )
                    }
                }
            }

            // Orders content area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))

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
                                color = OnSurfaceVariantDark,
                                fontSize = 14.sp
                            )
                        }
                    }
                    uiState.activeOrders.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay ordenes para este dia",
                                color = OnSurfaceVariantDark,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(uiState.activeOrders, key = { it.id }) { order ->
                                OrderListCard(
                                    order = order,
                                    onActionClick = {
                                        when (order.status) {
                                            OrderStatus.EnProceso,
                                            OrderStatus.Lavando,
                                            OrderStatus.Terminado -> viewModel.onCardAction(order)
                                            else -> onOrderClick(order.id)
                                        }
                                    },
                                    modifier = Modifier.clickable { onOrderClick(order.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Bottom sheets
    when (val sheet = uiState.sheetType) {
        is OrderSheetType.StaffSelection -> {
            val assignedStaffIds = sheet.order.staff.mapNotNull { it.staffId }.toSet()
            val unassignedStaff = uiState.availableStaff.filter { it.id !in assignedStaffIds }

            ChecklistBottomSheet(
                title = "¿Quién realizará el lavado?",
                items = unassignedStaff.map { ChecklistItem(id = it.id, label = it.fullName) },
                emptyMessage = "Todo el personal activo ya está asignado",
                buttonText = "Iniciar Lavado",
                buttonEnabled = { it.isNotEmpty() },
                isSubmitting = uiState.isSheetSubmitting,
                onDismiss = { viewModel.dismissSheet() },
                onConfirm = { selectedIds -> viewModel.submitStaffAndAdvance(sheet.order, selectedIds.toList()) }
            )
        }
        is OrderSheetType.QualityChecklist -> {
            val checklistItems = listOf(
                "Llantas y Aros Limpios",
                "Vidrios sin Manchas",
                "Filos de Puertas",
                "Secado sin Marcas",
                "Aspirado Completo",
                "Tablero, consola y puertas",
                "Encerado sin manchas",
                "Espejos secos"
            )

            ChecklistBottomSheet(
                title = "Checklist de Calidad",
                items = checklistItems.map { ChecklistItem(id = it, label = it) },
                buttonText = "Marcar como Terminado",
                buttonEnabled = { it.size == checklistItems.size },
                isSubmitting = uiState.isSheetSubmitting,
                onDismiss = { viewModel.dismissSheet() },
                onConfirm = { viewModel.submitQualityAndAdvance(sheet.order) }
            )
        }
        is OrderSheetType.Delivery -> DeliveryBottomSheet(
            paymentMethods = uiState.paymentMethods,
            isDelivering = uiState.isSheetSubmitting,
            onDismiss = { viewModel.dismissSheet() },
            onConfirm = { method, photos -> viewModel.submitDelivery(sheet.order, method, photos) }
        )
        is OrderSheetType.None -> {}
    }
}

private val EaseInOut = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
private const val PILL_ANIM_MS = 300

@Composable
private fun DateItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val animSpec = tween<Color>(durationMillis = PILL_ANIM_MS, easing = EaseInOut)
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) OrangePrimary else Color.Transparent,
        animationSpec = animSpec,
        label = "dateBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else OnSurfaceVariantDark,
        animationSpec = animSpec,
        label = "dateTxt"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = if (isSelected) 14.dp else 10.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isSelected) "${spanishDayFull(date.dayOfWeek)}, ${date.dayOfMonth}" else "${date.dayOfMonth}",
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )

            // Today indicator dot
            if (isToday && !isSelected) {
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary)
                )
            }
        }
    }
}

private fun spanishDayFull(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Lunes"
    DayOfWeek.TUESDAY -> "Martes"
    DayOfWeek.WEDNESDAY -> "Miercoles"
    DayOfWeek.THURSDAY -> "Jueves"
    DayOfWeek.FRIDAY -> "Viernes"
    DayOfWeek.SATURDAY -> "Sabado"
    DayOfWeek.SUNDAY -> "Domingo"
}

private fun spanishMonthName(month: Month): String = when (month) {
    Month.JANUARY -> "Enero"
    Month.FEBRUARY -> "Febrero"
    Month.MARCH -> "Marzo"
    Month.APRIL -> "Abril"
    Month.MAY -> "Mayo"
    Month.JUNE -> "Junio"
    Month.JULY -> "Julio"
    Month.AUGUST -> "Agosto"
    Month.SEPTEMBER -> "Septiembre"
    Month.OCTOBER -> "Octubre"
    Month.NOVEMBER -> "Noviembre"
    Month.DECEMBER -> "Diciembre"
}
