package com.example.carwash.presentation.screens.addorder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.carwash.domain.model.Service
import com.example.carwash.presentation.navigation.Screen
import com.example.carwash.presentation.viewmodel.AddOrderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ServicesScreen(navController: NavController, viewModel: AddOrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showServicesSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Temp selection state only lives while sheet is open
    var tempSelected by remember { mutableStateOf<List<Service>>(emptyList()) }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Servicios") },
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            // ---- Header + Add button ----
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Servicios seleccionados", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                        onClick = {
                            tempSelected = uiState.selectedServices
                            showServicesSheet = true
                        }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(" Agregar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- Selected services as chips (FlowRow) ----
            if (uiState.selectedServices.isEmpty()) {
                Text(
                        "No has seleccionado ningún servicio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    uiState.selectedServices.forEach { service ->
                        InputChip(
                                selected = true,
                                onClick = {},
                                label = { Text(service.name) },
                                trailingIcon = {
                                    Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Quitar ${service.name}",
                                            modifier =
                                                    Modifier.clickable {
                                                        viewModel.onServiceRemoved(service)
                                                    }
                                    )
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                    onClick = { navController.navigate(Screen.AddOrderObservations.route) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.selectedServices.isNotEmpty()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Siguiente")
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }

    // ---- Services Bottom Sheet ----
    if (showServicesSheet) {
        ModalBottomSheet(
                onDismissRequest = { showServicesSheet = false },
                sheetState = sheetState
        ) {
            Text(
                    "Seleccionar servicios",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider()
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.availableServices) { service ->
                    val isSelected = tempSelected.contains(service)
                    ListItem(
                            headlineContent = { Text(service.name) },
                            supportingContent = service.description?.let { { Text(it) } },
                            leadingContent = {
                                Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            tempSelected =
                                                    if (isSelected) {
                                                        tempSelected - service
                                                    } else {
                                                        tempSelected + service
                                                    }
                                        }
                                )
                            },
                            modifier =
                                    Modifier.clickable {
                                        tempSelected =
                                                if (isSelected) {
                                                    tempSelected - service
                                                } else {
                                                    tempSelected + service
                                                }
                                    }
                    )
                    HorizontalDivider()
                }
            }
            Button(
                    onClick = {
                        viewModel.onServicesConfirmed(tempSelected)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) showServicesSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) { Text("Confirmar (${tempSelected.size} seleccionados)") }
        }
    }
}
