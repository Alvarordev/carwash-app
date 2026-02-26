package com.example.carwash.presentation.screens.addorder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.carwash.presentation.navigation.Screen
import com.example.carwash.presentation.viewmodel.AddOrderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationsScreen(navController: NavController, viewModel: AddOrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showStaffSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Observaciones") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Atrás"
                                )
                            }
                        }
                )
            }
    ) { paddingValues ->
        Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---- Observations text field ----
            OutlinedTextField(
                    value = uiState.observations,
                    onValueChange = { viewModel.onObservationsChanged(it) },
                    label = { Text("Observaciones del cliente o del vehículo") },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    maxLines = 6,
                    placeholder = {
                        Text("Ej: Rayón en puerta derecha, cliente pide revisión de llantas...")
                    }
            )

            // ---- Staff assignment ----
            Text("Staff asignado", style = MaterialTheme.typography.titleMedium)

            if (uiState.selectedStaff != null) {
                InputChip(
                        selected = true,
                        onClick = {},
                        label = { Text(uiState.selectedStaff!!.fullName) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingIcon = {
                            Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Quitar staff",
                                    modifier = Modifier.clickable { viewModel.onStaffDeselected() }
                            )
                        }
                )
            }

            OutlinedButton(
                    onClick = { showStaffSheet = true },
                    modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Text(
                        if (uiState.selectedStaff == null) "  Asignar miembro del staff"
                        else "  Cambiar miembro del staff"
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                    onClick = { navController.navigate(Screen.AddOrderSummary.route) },
                    modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Siguiente")
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }

    // ---- Staff Bottom Sheet ----
    if (showStaffSheet) {
        ModalBottomSheet(onDismissRequest = { showStaffSheet = false }, sheetState = sheetState) {
            Text(
                    "Seleccionar miembro del staff",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider()

            if (uiState.availableStaff.isEmpty()) {
                Text(
                        "No hay staff disponible.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    items(uiState.availableStaff) { staff ->
                        ListItem(
                                headlineContent = { Text(staff.fullName) },
                                supportingContent = { Text(staff.role.name) },
                                leadingContent = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                },
                                modifier =
                                        Modifier.clickable {
                                            viewModel.onStaffSelected(staff)
                                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                                if (!sheetState.isVisible) showStaffSheet = false
                                            }
                                        }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
