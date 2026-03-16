package com.example.carwash.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.carwash.domain.model.Service
import com.example.carwash.ui.theme.OnSurfaceVariantDark
import com.example.carwash.ui.theme.OrangePrimary
import com.example.carwash.ui.theme.SurfaceDark
import kotlinx.coroutines.launch

/**
 * Reusable service picker bottom sheet used in both AddOrder and OrderDetails flows.
 *
 * @param services       All available services to pick from.
 * @param initialSelected Services that should be pre-checked when the sheet opens.
 * @param prices         Optional map of serviceId → resolved price (shown on the right).
 * @param onDismiss      Called when the sheet is dismissed without confirming.
 * @param onConfirm      Called with the final list of selected services.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicePickerSheet(
    services: List<Service>,
    initialSelected: List<Service>,
    prices: Map<String, Double> = emptyMap(),
    onDismiss: () -> Unit,
    onConfirm: (List<Service>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var tempSelected by remember { mutableStateOf(initialSelected) }

    val grouped = remember(services) {
        services.groupBy { it.category?.name ?: "Otros" }
    }

    val consumeScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = Offset(0f, available.y)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.9f)) {
            Text(
                "Seleccionar servicios",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(consumeScrollConnection)
            ) {
                grouped.forEach { (categoryName, categoryServices) ->
                    item(key = "header_$categoryName") {
                        Text(
                            categoryName.uppercase(),
                            color = OnSurfaceVariantDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                    items(categoryServices, key = { it.id }) { service ->
                        val isSelected = tempSelected.contains(service)
                        val iconRes = serviceIconDrawable(service.icon)
                        val serviceColor = service.color?.let { hex ->
                            runCatching { Color(hex.toColorInt()) }.getOrNull()
                        } ?: OnSurfaceVariantDark
                        val price = prices[service.id]

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tempSelected = if (isSelected) tempSelected - service else tempSelected + service
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    tempSelected = if (isSelected) tempSelected - service else tempSelected + service
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = OrangePrimary,
                                    uncheckedColor = OnSurfaceVariantDark
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            if (iconRes != null) {
                                Icon(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = null,
                                    tint = serviceColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = OnSurfaceVariantDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(service.name, color = serviceColor, fontSize = 14.sp)
                                service.description?.let {
                                    Text(it, color = OnSurfaceVariantDark, fontSize = 12.sp)
                                }
                            }
                            if (price != null && price > 0.0) {
                                Text(
                                    "S/ ${String.format("%.2f", price)}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
            Button(
                onClick = {
                    onConfirm(tempSelected)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text(
                    "Confirmar (${tempSelected.size} seleccionados)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
