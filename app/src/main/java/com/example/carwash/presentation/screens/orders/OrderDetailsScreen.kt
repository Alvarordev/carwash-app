package com.example.carwash.presentation.screens.orders

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.carwash.domain.model.OrderItem
import com.example.carwash.domain.model.OrderStaff
import com.example.carwash.domain.model.OrderStatus
import com.example.carwash.domain.model.OrderStatusHistory
import com.example.carwash.domain.model.PaymentMethod
import java.io.File
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import com.example.carwash.domain.model.Service
import com.example.carwash.domain.model.StaffMember
import com.example.carwash.domain.model.StaffRole
import com.example.carwash.presentation.components.serviceIconDrawable
import com.example.carwash.presentation.viewmodel.OrderDetailsViewModel
import com.example.carwash.ui.theme.BackgroundDark
import com.example.carwash.ui.theme.OnSurfaceVariantDark
import com.example.carwash.ui.theme.OrangePrimary
import com.example.carwash.ui.theme.SurfaceCardDark
import com.example.carwash.ui.theme.SurfaceDark
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit = onBack,
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showStaffPicker by remember { mutableStateOf(false) }
    var showServicesPicker by remember { mutableStateOf(false) }
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }

    val isLocked = uiState.order?.status == OrderStatus.Entregado || uiState.order?.status == OrderStatus.Anulado

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.clearSaveSuccess()
            Toast.makeText(context, "Cambios guardados correctamente", Toast.LENGTH_SHORT).show()
            onSaveSuccess()
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Text(
                    text = uiState.order?.orderNumber ?: "Detalle de Orden",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                if (!isLocked) {
                    Button(
                        onClick = { viewModel.saveChanges() },
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(Color.Transparent)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(color = OrangePrimary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Guardar", color = OrangePrimary, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = when {
                uiState.isLoading -> "loading"
                uiState.errorMessage != null && uiState.order == null -> "error"
                else -> "content"
            },
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
            label = "detailsContent",
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) { displayMode ->
            when (displayMode) {
                "loading" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangePrimary)
                }

                "error" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        uiState.errorMessage ?: "Error al cargar la orden",
                        color = OnSurfaceVariantDark,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }

                else -> {
                    val order = uiState.order ?: return@AnimatedContent

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        if (isLocked) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = OnSurfaceVariantDark, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Orden entregada · solo lectura", color = OnSurfaceVariantDark, fontSize = 12.sp)
                                }
                            }
                        }

                        item { SectionHeader(icon = Icons.Default.DirectionsCar, title = "VEHÍCULO") }
                        item {
                            val v = order.vehicle
                            DetailCard(rows = buildList {
                                v?.brand?.let { add("Marca" to it) }
                                v?.model?.let { add("Modelo" to it) }
                                v?.plate?.let { add("Placa" to it) }
                                v?.color?.let { add("Color" to it) }
                                if (isEmpty()) add("Orden" to order.orderNumber)
                            })
                        }

                        if (order.customer != null) {
                            item { SectionHeader(icon = Icons.Default.Person, title = "CLIENTE") }
                            item {
                                DetailCard(rows = buildList {
                                    add("Nombre" to order.customer.fullName)
                                    order.customer.phone?.let { add("Teléfono" to it) }
                                })
                            }
                        }

                        if (order.photos.isNotEmpty()) {
                            item { SectionHeader(icon = Icons.Default.Image, title = "GALERÍA DE FOTOS") }
                            item {
                                HorizontalPhotoStrip(
                                    photos = order.photos,
                                    onPhotoClick = { selectedPhotoUrl = it }
                                )
                            }
                        }

                        item { SectionHeader(icon = Icons.Default.Build, title = "SERVICIOS") }
                        if (uiState.pendingItems.isEmpty()) {
                            item {
                                Text(
                                    "Sin servicios asignados",
                                    color = OnSurfaceVariantDark,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        } else {
                            items(uiState.pendingItems, key = { it.id }) { item ->
                                ServiceItemRow(
                                    item = item,
                                    isLocked = isLocked,
                                    onRemove = { viewModel.setServices(
                                        uiState.pendingItems
                                            .filter { it.id != item.id }
                                            .mapNotNull { pi -> uiState.availableServices.find { s -> s.id == pi.serviceId } }
                                    )}
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                        if (!isLocked) {
                            item {
                                AddServiceRow(onClick = { showServicesPicker = true })
                                Spacer(Modifier.height(6.dp))
                            }
                        }

                        item { SectionHeader(icon = Icons.Default.Group, title = "PERSONAL ASIGNADO") }
                        items(uiState.pendingStaff, key = { it.id }) { staffEntry ->
                            StaffRow(
                                staffEntry = staffEntry,
                                isLocked = isLocked,
                                onRemove = { viewModel.removeStaff(staffEntry.id) },
                                modifier = Modifier.animateItem()
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        if (!isLocked) {
                            item {
                                AddStaffRow(onClick = { showStaffPicker = true })
                                Spacer(Modifier.height(6.dp))
                            }
                        }

                        item { SectionHeader(icon = Icons.Default.CheckCircle, title = "ESTADO") }
                        item {
                            StatusTimeline(
                                currentStatus = order.status,
                                statusHistory = order.statusHistory,
                                orderCreatedAt = order.createdAt,
                                pendingStatus = uiState.selectedStatus,
                                isLocked = isLocked,
                                onMarkNext = { viewModel.selectStatus(it) },
                                onUndo = { viewModel.undoStatus() }
                            )
                        }

                        uiState.errorMessage?.let { err ->
                            item {
                                Spacer(Modifier.height(8.dp))
                                Text(err, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                            }
                        }

                        item { Spacer(Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }

    if (showStaffPicker) {
        val assignedIds = uiState.pendingStaff.mapNotNull { it.staffId }.toSet()
        val available = uiState.availableStaff.filter { it.id !in assignedIds }
        ModalBottomSheet(
            onDismissRequest = { showStaffPicker = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = SurfaceDark
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("Seleccionar Personal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                if (available.isEmpty()) {
                    Text("Todo el personal activo ya está asignado", color = OnSurfaceVariantDark, fontSize = 14.sp, modifier = Modifier.padding(bottom = 32.dp))
                } else {
                    available.forEach { member ->
                        StaffPickerRow(member = member, onClick = {
                            viewModel.addStaff(member)
                            showStaffPicker = false
                        })
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showServicesPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()

        // Pre-select services already in pendingItems
        var tempSelected by remember(showServicesPicker) {
            mutableStateOf(
                uiState.pendingItems.mapNotNull { item ->
                    uiState.availableServices.find { it.id == item.serviceId }
                }
            )
        }

        ModalBottomSheet(
            onDismissRequest = { showServicesPicker = false },
            sheetState = sheetState,
            containerColor = SurfaceDark
        ) {
            Text("Seleccionar servicios", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.availableServices) { service ->
                    val isSelected = tempSelected.contains(service)
                    val iconRes = serviceIconDrawable(service.icon)
                    val serviceColor = service.color?.let { hex ->
                        runCatching { Color(hex.toColorInt()) }.getOrNull()
                    } ?: OnSurfaceVariantDark

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempSelected = if (isSelected) tempSelected - service else tempSelected + service }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (iconRes != null) {
                            Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = serviceColor, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = OnSurfaceVariantDark, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.name, color = serviceColor, fontSize = 14.sp)
                            service.description?.let { Text(it, color = OnSurfaceVariantDark, fontSize = 12.sp) }
                        }
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { tempSelected = if (isSelected) tempSelected - service else tempSelected + service },
                            colors = CheckboxDefaults.colors(checkedColor = OrangePrimary, uncheckedColor = OnSurfaceVariantDark)
                        )
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                }
            }
            Button(
                onClick = {
                    viewModel.setServices(tempSelected)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showServicesPicker = false
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("Confirmar (${tempSelected.size} seleccionados)", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    selectedPhotoUrl?.let { url ->
        Dialog(
            onDismissRequest = { selectedPhotoUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)).clickable { selectedPhotoUrl = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(model = url, contentDescription = "Foto", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth())
                IconButton(
                    onClick = { selectedPhotoUrl = null },
                    modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(8.dp)
                ) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    if (uiState.showDeliverySheet) {
        DeliveryBottomSheet(
            paymentMethods = uiState.paymentMethods,
            isDelivering = uiState.isDelivering,
            onDismiss = { viewModel.dismissDeliverySheet() },
            onConfirm = { method, photos -> viewModel.confirmDelivery(method, photos) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeliveryBottomSheet(
    paymentMethods: List<PaymentMethod>,
    isDelivering: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (paymentMethod: String, photoUris: List<Uri>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current

    // Camera launcher
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraUri != null && photoUris.size < 4) {
            photoUris = photoUris + cameraUri!!
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && photoUris.size < 4) {
            photoUris = photoUris + uri
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "Confirmar Entrega",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Payment method section
            Text(
                "Método de pago",
                color = OnSurfaceVariantDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            ) {
                paymentMethods.forEach { method ->
                    FilterChip(
                        selected = selectedMethod == method.name,
                        onClick = { selectedMethod = method.name },
                        label = { Text(method.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceCardDark,
                            labelColor = OnSurfaceVariantDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = Color.Transparent,
                            selectedBorderColor = Color.Transparent,
                            enabled = true,
                            selected = selectedMethod == method.name
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Photos section
            Text(
                "Foto de entrega (opcional)",
                color = OnSurfaceVariantDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val file = File(context.cacheDir, "delivery_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                        cameraUri = uri
                        cameraLauncher.launch(uri)
                    },
                    enabled = photoUris.size < 4,
                    border = ButtonDefaults.outlinedButtonBorder(enabled = photoUris.size < 4),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cámara")
                }
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    enabled = photoUris.size < 4,
                    border = ButtonDefaults.outlinedButtonBorder(enabled = photoUris.size < 4),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Galería")
                }
            }

            if (photoUris.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    photoUris.forEachIndexed { index, uri ->
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            IconButton(
                                onClick = { photoUris = photoUris.toMutableList().also { it.removeAt(index) } },
                                modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar", tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Confirm button
            Button(
                onClick = { selectedMethod?.let { onConfirm(it, photoUris) } },
                enabled = selectedMethod != null && !isDelivering,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                if (isDelivering) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Confirmar Entrega", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}


@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = OnSurfaceVariantDark, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(title, color = OnSurfaceVariantDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
    }
}

@Composable
private fun DetailCard(rows: List<Pair<String, String>>) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceCardDark)) {
        rows.forEachIndexed { index, (key, value) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(key, color = OnSurfaceVariantDark, fontSize = 14.sp)
                Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
            }
            if (index < rows.size - 1) HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
        }
    }
}

@Composable
private fun HorizontalPhotoStrip(photos: List<String>, onPhotoClick: (String) -> Unit) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        photos.forEach { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)).clickable { onPhotoClick(url) }
            )
        }
    }
}

@Composable
private fun ServiceItemRow(item: OrderItem, isLocked: Boolean, onRemove: () -> Unit) {
    val serviceIconRes = serviceIconDrawable(item.serviceIcon)
    val serviceColor = item.serviceColor?.let { hex ->
        runCatching { Color(hex.toColorInt()) }.getOrNull()
    } ?: OnSurfaceVariantDark

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardDark)
            .padding(start = 16.dp, end = if (isLocked) 16.dp else 4.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (serviceIconRes != null) {
            Icon(painter = painterResource(id = serviceIconRes), tint = serviceColor, contentDescription = null, modifier = Modifier.size(16.dp))
        } else {
            Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = OnSurfaceVariantDark, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.serviceName, color = serviceColor, fontWeight = FontWeight.Normal, fontSize = 14.sp)
            if (item.quantity > 1) Text("x${item.quantity}", color = OnSurfaceVariantDark, fontSize = 12.sp)
        }
        if (item.subtotal > 0) {
            Text("S/ ${"%.2f".format(item.subtotal)}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
        if (!isLocked) {
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Quitar", tint = OnSurfaceVariantDark, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AddServiceRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text("Agregar servicio", color = OrangePrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun StaffRow(staffEntry: OrderStaff, isLocked: Boolean, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardDark)
            .padding(start = 16.dp, end = if (isLocked) 16.dp else 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = OnSurfaceVariantDark, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(staffEntry.staffName, color = Color.White, fontSize = 14.sp)
            staffEntry.roleSnapshot?.let { Text(it.toDisplayName(), color = OnSurfaceVariantDark, fontSize = 12.sp) }
        }
        if (!isLocked) {
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Quitar", tint = OnSurfaceVariantDark, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AddStaffRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text("Agregar Personal", color = OrangePrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun StatusTimeline(
    currentStatus: OrderStatus,
    statusHistory: List<OrderStatusHistory>,
    orderCreatedAt: OffsetDateTime,
    pendingStatus: OrderStatus?,
    isLocked: Boolean,
    onMarkNext: (OrderStatus) -> Unit,
    onUndo: () -> Unit
) {
    val timelineStatuses = listOf(
        OrderStatus.EnProceso,
        OrderStatus.Lavando,
        OrderStatus.Terminado,
        OrderStatus.Entregado
    )
    val currentIndex = timelineStatuses.indexOf(currentStatus).coerceAtLeast(0)
    val nextStatus = when (currentStatus) {
        OrderStatus.EnProceso -> OrderStatus.Lavando
        OrderStatus.Lavando -> OrderStatus.Terminado
        OrderStatus.Terminado -> OrderStatus.Entregado
        else -> null
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCardDark)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            timelineStatuses.forEachIndexed { index, status ->
                val isPast = index < currentIndex
                val isCurrent = index == currentIndex
                val isPending = status == pendingStatus
                val isReached = isPast || isCurrent

                val dotColor by animateColorAsState(
                    targetValue = when {
                        isPending -> OrangePrimary
                        isReached -> OrangePrimary
                        else -> OnSurfaceVariantDark.copy(alpha = 0.3f)
                    },
                    animationSpec = tween(300), label = "dot_$index"
                )

                val historyEntry = when {
                    status == OrderStatus.EnProceso && isReached -> {
                        statusHistory.find { it.status == status } ?: OrderStatusHistory(
                            id = "", orderId = "", status = status, createdAt = orderCreatedAt
                        )
                    }
                    isReached -> statusHistory.find { it.status == status }
                    else -> null
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dot
                    Box(
                        modifier = Modifier.size(12.dp).clip(CircleShape).background(dotColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPast) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(8.dp))
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    // Status name
                    Text(
                        text = status.toDisplayLabel(),
                        color = if (isReached || isPending) Color.White else OnSurfaceVariantDark.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )

                    // Date + time
                    if (historyEntry != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = historyEntry.createdAt.format(dateFormatter),
                                color = OnSurfaceVariantDark,
                                fontSize = 12.sp
                            )
                            Text(
                                text = historyEntry.createdAt.format(timeFormatter),
                                color = OnSurfaceVariantDark.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Vertical line between dots
                if (index < timelineStatuses.size - 1) {
                    val lineColor by animateColorAsState(
                        targetValue = if (index < currentIndex || (isCurrent && isPending))
                            OrangePrimary
                        else
                            OnSurfaceVariantDark.copy(alpha = 0.15f),
                        animationSpec = tween(300), label = "line_$index"
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .width(2.dp)
                            .height(28.dp)
                            .background(lineColor)
                    )
                }
            }
        }

        // Action button (only if not locked)
        if (!isLocked && nextStatus != null) {
            Spacer(Modifier.height(10.dp))
            if (pendingStatus != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Siguiente: ${nextStatus.toDisplayLabel()}",
                        color = OrangePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onUndo) {
                        Text("Deshacer", color = OnSurfaceVariantDark, fontSize = 13.sp)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCardDark)
                        .clickable { onMarkNext(nextStatus) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Marcar como ${nextStatus.toDisplayLabel()}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StaffPickerRow(member: StaffMember, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceCardDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = OnSurfaceVariantDark, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(member.fullName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(member.role.toDisplayName(), color = OnSurfaceVariantDark, fontSize = 12.sp)
        }
    }
}

private fun OrderStatus.toDisplayLabel(): String = when (this) {
    OrderStatus.EnProceso -> "En Proceso"
    OrderStatus.Lavando -> "Lavando"
    OrderStatus.Terminado -> "Terminado"
    OrderStatus.Entregado -> "Entregado"
    OrderStatus.Anulado -> "Anulado"
}

private fun StaffRole.toDisplayName(): String = when (this) {
    StaffRole.Admin -> "Administrador"
    StaffRole.Washer -> "Lavador"
    StaffRole.Cashier -> "Cajero"
    StaffRole.Supervisor -> "Supervisor"
}
