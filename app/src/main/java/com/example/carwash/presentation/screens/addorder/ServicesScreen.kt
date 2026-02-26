package com.example.carwash.presentation.screens.addorder

import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.carwash.presentation.navigation.ADD_ORDER_GRAPH_ROUTE
import com.example.carwash.presentation.navigation.Screen
import com.example.carwash.presentation.viewmodel.AddOrderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(navController: NavController, viewModel: AddOrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var openServicesSheet by remember { mutableStateOf(false) }
    var openPromosSheet by remember { mutableStateOf(false) }
    val servicesSheetState = rememberModalBottomSheetState()
    val promosSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paso 3: Servicios y Finalizar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { openServicesSheet = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Seleccionar Servicios (${uiState.selectedServices.size})")
            }

            OutlinedButton(
                onClick = { openPromosSheet = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(uiState.selectedPromotion?.name ?: "Aplicar Promoción")
            }

            OutlinedTextField(
                value = uiState.observations,
                onValueChange = { viewModel.onObservationsChanged(it) },
                label = { Text("Observaciones") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Button(
                onClick = {
                    viewModel.createOrder()
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(ADD_ORDER_GRAPH_ROUTE) {
                            inclusive = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finalizar Orden")
            }
        }
    }

    if (openServicesSheet) {
        ModalBottomSheet(
            onDismissRequest = { openServicesSheet = false },
            sheetState = servicesSheetState
        ) {
            val tempSelectedServices = remember { mutableStateOf(uiState.selectedServices) }
            
            Column {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.availableServices) { service ->
                        ListItem(
                            headlineContent = { Text(service.name) },
                            leadingContent = {
                                Checkbox(
                                    checked = tempSelectedServices.value.contains(service),
                                    onCheckedChange = {
                                        val currentServices = tempSelectedServices.value.toMutableList()
                                        if (currentServices.contains(service)) {
                                            currentServices.remove(service)
                                        } else {
                                            currentServices.add(service)
                                        }
                                        tempSelectedServices.value = currentServices
                                    }
                                )
                            }
                        )
                    }
                }
                Button(
                    onClick = {
                        viewModel.onServicesConfirmed(tempSelectedServices.value)
                        scope.launch {
                            servicesSheetState.hide()
                        }.invokeOnCompletion {
                            if (!servicesSheetState.isVisible) {
                                openServicesSheet = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("Confirmar")
                }
            }
        }
    }

    if (openPromosSheet) {
        ModalBottomSheet(
            onDismissRequest = { openPromosSheet = false },
            sheetState = promosSheetState
        ) {
            LazyColumn {
                items(uiState.availablePromotions) { promo ->
                    ListItem(
                        headlineContent = { Text(promo.name) },
                        supportingContent = { promo.description?.let { Text(it) } },
                        modifier = Modifier.clickable {
                            viewModel.onPromotionSelected(promo)
                             scope.launch {
                                promosSheetState.hide()
                            }.invokeOnCompletion {
                                if (!promosSheetState.isVisible) {
                                    openPromosSheet = false
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}