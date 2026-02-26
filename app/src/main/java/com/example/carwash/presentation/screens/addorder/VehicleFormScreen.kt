package com.example.carwash.presentation.screens.addorder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.carwash.presentation.navigation.Screen
import com.example.carwash.presentation.viewmodel.AddOrderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFormScreen(navController: NavController, viewModel: AddOrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showVehicleTypeSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val isFormValid =
            uiState.plate.isNotBlank() &&
                    uiState.brand.isNotBlank() &&
                    uiState.color.isNotBlank() &&
                    uiState.vehicleType != null

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Datos del Vehículo") },
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                    value = uiState.plate,
                    onValueChange = { viewModel.onPlateChanged(it.uppercase()) },
                    label = { Text("Placa *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions =
                            KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )
            OutlinedTextField(
                    value = uiState.brand,
                    onValueChange = { viewModel.onBrandChanged(it) },
                    label = { Text("Marca *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
            )
            OutlinedTextField(
                    value = uiState.model,
                    onValueChange = { viewModel.onModelChanged(it) },
                    label = { Text("Modelo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
            )
            OutlinedTextField(
                    value = uiState.color,
                    onValueChange = { viewModel.onColorChanged(it) },
                    label = { Text("Color *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
            )

            // Vehicle type selector — clickable box avoids disabled+clickable conflict
            OutlinedTextField(
                    value = uiState.vehicleType?.name ?: "",
                    onValueChange = {},
                    label = { Text("Tipo de Vehículo *") },
                    modifier = Modifier.fillMaxWidth().clickable { showVehicleTypeSheet = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { showVehicleTypeSheet = true }
                        )
                    },
                    enabled = false
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                    onClick = { navController.navigate(Screen.AddOrderServices.route) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Siguiente")
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }

    if (showVehicleTypeSheet) {
        ModalBottomSheet(
                onDismissRequest = { showVehicleTypeSheet = false },
                sheetState = sheetState
        ) {
            Text(
                    "Tipo de Vehículo",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
            HorizontalDivider()
            LazyColumn {
                items(uiState.availableVehicleTypes) { type ->
                    ListItem(
                            headlineContent = { Text(type.name) },
                            supportingContent = type.description?.let { { Text(it) } },
                            modifier =
                                    Modifier.clickable {
                                        viewModel.onVehicleTypeSelected(type)
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            if (!sheetState.isVisible) showVehicleTypeSheet = false
                                        }
                                    }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
