package com.example.carwash.presentation.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.carwash.ui.theme.OrangePrimary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

@Composable
fun DashboardScreen(
    onOrderClick: (String) -> Unit = {},
    onViewAll: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val today = remember { LocalDate.now(ZoneId.of("America/Lima")) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
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
            uiState.isEmpty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.hasDateFilter) "No hay ordenes para esta fecha"
                               else "No hay ordenes recientes",
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
                ) {
                    uiState.sections.forEach { section ->
                        item(key = "header_${section.date}") {
                            Text(
                                text = formatSectionDate(section.date, today),
                                color = colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.3.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(section.orders, key = { it.id }) { order ->
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

private fun formatSectionDate(date: LocalDate, today: LocalDate): String {
    val dayName = when {
        date == today -> "Hoy"
        date == today.minusDays(1) -> "Ayer"
        else -> spanishDayFull(date.dayOfWeek)
    }
    val monthName = spanishMonthName(date.month)
    return "$dayName, $monthName ${date.dayOfMonth}"
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
