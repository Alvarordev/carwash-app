package com.example.carwash.presentation.screens.addorder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.carwash.presentation.navigation.Screen
import com.example.carwash.presentation.viewmodel.AddOrderViewModel
import com.example.carwash.ui.theme.OrangePrimary
import com.example.carwash.ui.theme.StatusDone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(navController: NavController, viewModel: AddOrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    val customerFound = uiState.foundCustomer != null
    val canContinue = when {
        uiState.isSearchingCustomer -> false
        customerFound -> true
        uiState.customerPhone.isNotBlank() && uiState.customerFirstName.isNotBlank() -> true
        else -> false
    }

    val darkFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = OrangePrimary,
        unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.6f),
        focusedLabelColor = OrangePrimary,
        unfocusedLabelColor = colorScheme.onSurfaceVariant,
        cursorColor = OrangePrimary,
        focusedTextColor = colorScheme.onSurface,
        unfocusedTextColor = colorScheme.onSurface,
        disabledTextColor = colorScheme.onSurface.copy(alpha = 0.7f),
        disabledBorderColor = StatusDone.copy(alpha = 0.4f),
        disabledLabelColor = StatusDone.copy(alpha = 0.7f),
        disabledContainerColor = Color.Transparent,
    )

    Scaffold(containerColor = colorScheme.background) { paddingValues ->
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = colorScheme.onBackground)
                }
                Text(
                    text = "Cliente",
                    color = colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "DATOS DEL CLIENTE",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                }

                OutlinedTextField(
                    value = uiState.customerPhone,
                    onValueChange = { viewModel.onCustomerPhoneChanged(it) },
                    label = { Text("Teléfono *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    trailingIcon = {
                        if (uiState.isSearchingCustomer) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = OrangePrimary
                            )
                        } else if (customerFound) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusDone,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = darkFieldColors
                )

                if (customerFound) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(StatusDone.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(StatusDone.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = StatusDone,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Cliente encontrado en el sistema",
                            color = StatusDone,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (uiState.customerPhone.length >= 7 && !uiState.isSearchingCustomer) {
                    Text(
                        "Número no registrado. Completa los datos para crear el cliente.",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }

                OutlinedTextField(
                    value = uiState.customerFirstName,
                    onValueChange = { if (!customerFound) viewModel.onCustomerFirstNameChanged(it) },
                    label = { Text(if (customerFound) "Nombre" else "Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !customerFound,
                    colors = darkFieldColors
                )

                OutlinedTextField(
                    value = uiState.customerLastName,
                    onValueChange = { if (!customerFound) viewModel.onCustomerLastNameChanged(it) },
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !customerFound,
                    colors = darkFieldColors
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (customerFound) viewModel.confirmFoundCustomer()
                        navController.navigate(Screen.AddOrderServices.route)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = canContinue,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text(
                        text = if (customerFound) "Confirmar y Continuar" else "Continuar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                TextButton(
                    onClick = {
                        viewModel.onCustomerSkipped()
                        navController.navigate(Screen.AddOrderServices.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Omitir", color = colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
