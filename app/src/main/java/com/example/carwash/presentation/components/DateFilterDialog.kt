package com.example.carwash.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.carwash.R
import com.example.carwash.presentation.viewmodel.DateFilterMode
import com.example.carwash.ui.theme.OrangePrimary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterDialog(
    currentMode: DateFilterMode,
    onModeSelected: (DateFilterMode) -> Unit,
    onDismiss: () -> Unit
) {
    var showCalendarPicker by remember { mutableStateOf(false) }

    if (showCalendarPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showCalendarPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        onModeSelected(DateFilterMode.SpecificDate(date))
                    }
                    showCalendarPicker = false
                }) {
                    Text("Seleccionar", color = OrangePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalendarPicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        val colorScheme = MaterialTheme.colorScheme

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colorScheme.surface)
                .border(1.dp, colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "Filtrar por fecha",
                color = colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(20.dp))

            // 2x2 grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateFilterOption(
                    label = "Hoy",
                    badgeText = "1",
                    isSelected = currentMode is DateFilterMode.Today,
                    onClick = { onModeSelected(DateFilterMode.Today) },
                    modifier = Modifier.weight(1f)
                )
                DateFilterOption(
                    label = "Semana",
                    badgeText = "7",
                    isSelected = currentMode is DateFilterMode.Week,
                    onClick = { onModeSelected(DateFilterMode.Week) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateFilterOption(
                    label = "Mes",
                    badgeText = "30",
                    isSelected = currentMode is DateFilterMode.Month,
                    onClick = { onModeSelected(DateFilterMode.Month) },
                    modifier = Modifier.weight(1f)
                )
                DateFilterOption(
                    label = "Elegir dia",
                    badgeText = null,
                    isSelected = currentMode is DateFilterMode.SpecificDate,
                    onClick = { showCalendarPicker = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DateFilterOption(
    label: String,
    badgeText: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val borderColor = if (isSelected) OrangePrimary else colorScheme.outline.copy(alpha = 0.3f)
    val containerColor = if (isSelected) OrangePrimary.copy(alpha = 0.08f) else Color.Transparent
    val iconTint = if (isSelected) OrangePrimary else colorScheme.onSurfaceVariant
    val textColor = if (isSelected) colorScheme.onSurface else colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp, horizontal = 8.dp)
    ) {
        // Calendar icon with overlaid number badge
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(
                    id = if (isSelected) R.drawable.calendar_fill else R.drawable.calendar
                ),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(36.dp)
            )
            if (badgeText != null) {
                Text(
                    text = badgeText,
                    color = iconTint,
                    fontSize = if (badgeText.length > 1) 11.sp else 13.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
