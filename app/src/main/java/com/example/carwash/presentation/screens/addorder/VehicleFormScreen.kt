package com.example.carwash.presentation.screens.addorder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import com.example.carwash.presentation.navigation.Screen
import com.example.carwash.presentation.viewmodel.AddOrderViewModel
import com.example.carwash.ui.theme.BackgroundDark
import com.example.carwash.ui.theme.OnSurfaceVariantDark
import com.example.carwash.ui.theme.OrangePrimary
import com.example.carwash.ui.theme.SurfaceCardDark
import com.example.carwash.ui.theme.SurfaceDark
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

    LaunchedEffect(uiState.vehicleAnalyzed) {
        if (uiState.vehicleAnalyzed) {
            snackbarHostState.showSnackbar("Datos del vehículo cargados", duration = SnackbarDuration.Short)
            viewModel.onVehicleAnalyzedShown()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by if (uiState.isAnalyzingVehicle) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "borderAlpha"
        )
    } else {
        infiniteTransition.animateFloat(
            initialValue = 1f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Restart), label = "borderAlphaStatic"
        )
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = OrangePrimary.copy(alpha = pulseAlpha),
        unfocusedBorderColor = Color.White.copy(alpha = 0.15f * pulseAlpha),
        focusedLabelColor = OrangePrimary,
        unfocusedLabelColor = OnSurfaceVariantDark,
        cursorColor = OrangePrimary,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
    )

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                }
                Text(
                    text = "Datos del Vehículo",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Analysis progress bar
                AnimatedVisibility(
                    visible = uiState.isAnalyzingVehicle,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = OrangePrimary,
                            trackColor = OrangePrimary.copy(alpha = 0.2f)
                        )
                        Text(
                            "Analizando vehículo...",
                            color = OnSurfaceVariantDark,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.plate,
                    onValueChange = { viewModel.onPlateChanged(it.uppercase()) },
                    label = { Text("Placa *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = uiState.brand,
                    onValueChange = { viewModel.onBrandChanged(it) },
                    label = { Text("Marca *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = uiState.model,
                    onValueChange = { viewModel.onModelChanged(it) },
                    label = { Text("Modelo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = uiState.color,
                    onValueChange = { viewModel.onColorChanged(it) },
                    label = { Text("Color *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = uiState.vehicleType?.name ?: "",
                    onValueChange = {},
                    label = { Text("Tipo de Vehículo *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showVehicleTypeSheet = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown, contentDescription = null,
                            tint = OnSurfaceVariantDark,
                            modifier = Modifier.clickable { showVehicleTypeSheet = true }
                        )
                    },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.White.copy(alpha = 0.15f),
                        disabledLabelColor = OnSurfaceVariantDark,
                        disabledTextColor = Color.White,
                        disabledTrailingIconColor = OnSurfaceVariantDark,
                        disabledContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { navController.navigate(Screen.AddOrderCustomer.route) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = isFormValid,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    if (uiState.isAnalyzingVehicle) {
                        CircularProgressIndicator(
                            color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp
                        )
                    } else {
                        Text("Siguiente", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showVehicleTypeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVehicleTypeSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceDark
        ) {
            Text(
                "Tipo de Vehículo",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
            LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
                items(uiState.availableVehicleTypes) { type ->
                    ListItem(
                        headlineContent = { Text(type.name, color = Color.White) },
                        supportingContent = type.description?.let { { Text(it, color = OnSurfaceVariantDark) } },
                        leadingContent = {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = OnSurfaceVariantDark)
                        },
                        modifier = Modifier.clickable {
                            viewModel.onVehicleTypeSelected(type)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) showVehicleTypeSheet = false
                            }
                        }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                }
            }
        }
    }
}
