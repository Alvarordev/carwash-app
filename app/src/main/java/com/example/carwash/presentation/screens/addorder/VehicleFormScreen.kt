package com.example.carwash.presentation.screens.addorder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
    val snackbarHostState = remember { SnackbarHostState() }

    val isFormValid =
            uiState.plate.isNotBlank() &&
                    uiState.brand.isNotBlank() &&
                    uiState.color.isNotBlank() &&
                    uiState.vehicleType != null

    // Snackbar when analysis completes
    LaunchedEffect(uiState.vehicleAnalyzed) {
        if (uiState.vehicleAnalyzed) {
            snackbarHostState.showSnackbar(
                    message = "Datos del vehículo cargados",
                    duration = SnackbarDuration.Short
            )
            viewModel.onVehicleAnalyzedShown()
        }
    }

    // Pulsing border animation while analyzing
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by if (uiState.isAnalyzingVehicle) {
        infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                label = "borderAlpha"
        )
    } else {
        infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Restart),
                label = "borderAlphaStatic"
        )
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = pulseAlpha),
    )

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
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Loading indicator while analyzing
            AnimatedVisibility(
                    visible = uiState.isAnalyzingVehicle,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                            "Analizando vehículo...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 0.dp, vertical = 4.dp)
                    )
                }
            }

            OutlinedTextField(
                    value = uiState.plate,
                    onValueChange = { viewModel.onPlateChanged(it.uppercase()) },
                    label = { Text("Placa *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions =
                            KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = fieldColors
            )
            OutlinedTextField(
                    value = uiState.brand,
                    onValueChange = { viewModel.onBrandChanged(it) },
                    label = { Text("Marca *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors
            )
            OutlinedTextField(
                    value = uiState.model,
                    onValueChange = { viewModel.onModelChanged(it) },
                    label = { Text("Modelo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors
            )
            OutlinedTextField(
                    value = uiState.color,
                    onValueChange = { viewModel.onColorChanged(it) },
                    label = { Text("Color *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors
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
