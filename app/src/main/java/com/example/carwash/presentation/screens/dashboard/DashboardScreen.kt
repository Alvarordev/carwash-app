package com.example.carwash.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carwash.domain.model.Order
import com.example.carwash.domain.model.OrderStatus
import com.example.carwash.domain.model.StaffMember
import com.example.carwash.presentation.components.DeliveryBottomSheet
import com.example.carwash.presentation.components.OrderListCard
import com.example.carwash.presentation.viewmodel.DashboardViewModel
import com.example.carwash.presentation.viewmodel.OrderSheetType
import com.example.carwash.ui.theme.BackgroundDark
import com.example.carwash.ui.theme.OnSurfaceVariantDark
import com.example.carwash.ui.theme.OrangePrimary
import com.example.carwash.ui.theme.SurfaceCardDark
import com.example.carwash.ui.theme.SurfaceDark
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

private val DayPillSelected = Color(0xFF2979FF)
private val BorderColor = Color(0xFF414141)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddOrder: () -> Unit,
    onOrderClick: (String) -> Unit = {},
    onViewAll: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = remember { LocalDate.now(ZoneId.of("America/Lima")) }
    val last7Days = remember { (6 downTo 0).map { today.minusDays(it.toLong()) } }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Header
            Text(
                text = if (uiState.isToday) "Ordenes de Hoy" else "Ordenes del ${formatDateShort(uiState.selectedDate)}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = "Bienvenido, ${uiState.staffName}!",
                color = OnSurfaceVariantDark,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                last7Days.forEach { date ->
                    DayPill(
                        dayName = spanishDayAbbrev(date.dayOfWeek),
                        dayNumber = date.dayOfMonth,
                        selected = date == uiState.selectedDate,
                        onClick = { viewModel.selectDate(date) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    label = "Completados",
                    value = "${uiState.completedCount}",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    label = "Tiempo promedio",
                    value = uiState.averageServiceTimeMinutes?.let { "${it}m" } ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Orders list
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
                            text = "No hay órdenes para este día",
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

    // Bottom sheets
    when (val sheet = uiState.sheetType) {
        is OrderSheetType.StaffSelection -> StaffSelectionSheet(
            order = sheet.order,
            availableStaff = uiState.availableStaff,
            isSubmitting = uiState.isSheetSubmitting,
            onDismiss = { viewModel.dismissSheet() },
            onConfirm = { selectedIds -> viewModel.submitStaffAndAdvance(sheet.order, selectedIds) }
        )
        is OrderSheetType.QualityChecklist -> QualityChecklistSheet(
            isSubmitting = uiState.isSheetSubmitting,
            onDismiss = { viewModel.dismissSheet() },
            onConfirm = { viewModel.submitQualityAndAdvance(sheet.order) }
        )
        is OrderSheetType.Delivery -> DeliveryBottomSheet(
            paymentMethods = uiState.paymentMethods,
            isDelivering = uiState.isSheetSubmitting,
            onDismiss = { viewModel.dismissSheet() },
            onConfirm = { method, photos -> viewModel.submitDelivery(sheet.order, method, photos) }
        )
        is OrderSheetType.None -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StaffSelectionSheet(
    order: Order,
    availableStaff: List<StaffMember>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val assignedStaffIds = order.staff.mapNotNull { it.staffId }.toSet()
    val unassignedStaff = availableStaff.filter { it.id !in assignedStaffIds }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceDark
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "¿Quién realizará el lavado?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (unassignedStaff.isEmpty()) {
                Text(
                    "Todo el personal activo ya está asignado",
                    color = OnSurfaceVariantDark,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                unassignedStaff.forEach { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedIds = if (member.id in selectedIds) {
                                    selectedIds - member.id
                                } else {
                                    selectedIds + member.id
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = member.id in selectedIds,
                            onCheckedChange = {
                                selectedIds = if (member.id in selectedIds) {
                                    selectedIds - member.id
                                } else {
                                    selectedIds + member.id
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = OrangePrimary,
                                uncheckedColor = OnSurfaceVariantDark
                            )
                        )
                        Text(
                            text = member.fullName,
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onConfirm(selectedIds.toList()) },
                enabled = selectedIds.isNotEmpty() && !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Iniciar Lavado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QualityChecklistSheet(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
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
    var checkedItems by remember { mutableStateOf<Set<String>>(emptySet()) }
    val allChecked = checkedItems.size == checklistItems.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceDark
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "Checklist de Calidad",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            checklistItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            checkedItems = if (item in checkedItems) checkedItems - item else checkedItems + item
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item in checkedItems,
                        onCheckedChange = {
                            checkedItems = if (item in checkedItems) checkedItems - item else checkedItems + item
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = OrangePrimary,
                            uncheckedColor = OnSurfaceVariantDark
                        )
                    )
                    Text(
                        text = item,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onConfirm,
                enabled = allChecked && !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Marcar como Terminado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RowScope.DayPill(
    dayName: String,
    dayNumber: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) DayPillSelected else SurfaceCardDark

    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .then(
                if (!selected) Modifier.border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = dayName,
            fontSize = 11.sp,
            color = if (selected) Color.White else OnSurfaceVariantDark
        )
        Text(
            text = "$dayNumber",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 12  .sp
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardDark)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = OnSurfaceVariantDark,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

private fun spanishDayAbbrev(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Lun"
    DayOfWeek.TUESDAY -> "Mar"
    DayOfWeek.WEDNESDAY -> "Mié"
    DayOfWeek.THURSDAY -> "Jue"
    DayOfWeek.FRIDAY -> "Vie"
    DayOfWeek.SATURDAY -> "Sáb"
    DayOfWeek.SUNDAY -> "Dom"
}

private fun formatDateShort(date: LocalDate): String {
    val day = spanishDayAbbrev(date.dayOfWeek)
    return "$day ${date.dayOfMonth}"
}
