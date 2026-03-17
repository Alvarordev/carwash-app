package com.example.carwash.presentation.screens.addorder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.carwash.presentation.navigation.Screen
import com.example.carwash.presentation.viewmodel.AddOrderViewModel
import com.example.carwash.ui.theme.OrangePrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationsScreen(navController: NavController, viewModel: AddOrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    var showStaffSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(containerColor = colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Top bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = colorScheme.onBackground)
                }
                Text(
                    text = "Observaciones",
                    color = colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            // ── Body ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Observations section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notes, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("OBSERVACIONES", color = colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
                    }

                    OutlinedTextField(
                        value = uiState.observations,
                        onValueChange = { viewModel.onObservationsChanged(it) },
                        placeholder = { Text("Ej: Rayón en puerta derecha, cliente pide revisión de llantas...", color = colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.6f),
                            cursorColor = OrangePrimary,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                        )
                    )
                }

                // Staff section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("PERSONAL ASIGNADO", color = colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
                    }

                    if (uiState.selectedStaff != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorScheme.surface)
                                .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(uiState.selectedStaff!!.fullName, color = colorScheme.onSurface, fontSize = 14.sp)
                                Text(uiState.selectedStaff!!.role.name, color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                            IconButton(onClick = { viewModel.onStaffDeselected() }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Add / Change staff row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.surface)
                            .clickable { showStaffSheet = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (uiState.selectedStaff == null) "Asignar personal" else "Cambiar personal",
                            color = OrangePrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { navController.navigate(Screen.AddOrderSummary.route) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Siguiente", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // ── Staff bottom sheet ───────────────────────────────────────────────────
    if (showStaffSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStaffSheet = false },
            sheetState = sheetState,
            containerColor = colorScheme.surface
        ) {
            Text(
                "Seleccionar personal",
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.35f), thickness = 0.5.dp)

            if (uiState.availableStaff.isEmpty()) {
                Text(
                    "No hay personal disponible.",
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
                    items(uiState.availableStaff) { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onStaffSelected(member)
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        if (!sheetState.isVisible) showStaffSheet = false
                                    }
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(member.fullName, color = colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(member.role.name, color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                        HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.35f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
