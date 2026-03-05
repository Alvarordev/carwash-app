package com.example.carwash.presentation.screens.orders

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.carwash.domain.model.OrderItem
import com.example.carwash.domain.model.OrderStaff
import com.example.carwash.domain.model.OrderStatus
import com.example.carwash.domain.model.StaffMember
import com.example.carwash.domain.model.StaffRole
import com.example.carwash.presentation.viewmodel.OrderDetailsViewModel
import com.example.carwash.ui.theme.BackgroundDark
import com.example.carwash.ui.theme.OnSurfaceVariantDark
import com.example.carwash.ui.theme.OrangePrimary
import com.example.carwash.ui.theme.SurfaceCardDark
import com.example.carwash.ui.theme.SurfaceDark

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
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }

    println(uiState.order?.photos)

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
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Text(
                    text = uiState.order?.orderNumber ?: "Detalle de Orden",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = Color.White)
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
            transitionSpec = {
                fadeIn(tween(250)) togetherWith fadeOut(tween(200))
            },
            label = "detailsContent",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { displayMode ->
            when (displayMode) {
                "loading" -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangePrimary)
                }

                "error" -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Error al cargar la orden",
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
                        // ── Vehículo ─────────────────────────────────────────
                        item {
                            SectionHeader(
                                icon = Icons.Default.DirectionsCar,
                                title = "VEHÍCULO"
                            )
                        }
                        item {
                            val v = order.vehicle
                            val rows = buildList {
                                v?.brand?.let { add("Marca" to it) }
                                v?.model?.let { add("Modelo" to it) }
                                v?.plate?.let { add("Placa" to it) }
                                v?.color?.let { add("Color" to it) }
                                if (isEmpty()) add("Orden" to order.orderNumber)
                            }
                            DetailCard(rows = rows)
                        }

                        // ── Cliente ───────────────────────────────────────────
                        if (order.customer != null) {
                            item {
                                SectionHeader(icon = Icons.Default.Person, title = "CLIENTE")
                            }
                            item {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(300))
                                ) {
                                    val rows = buildList {
                                        add("Nombre" to order.customer.fullName)
                                        order.customer.phone?.let { add("Teléfono" to it) }
                                    }
                                    DetailCard(rows = rows)
                                }
                            }
                        }

                        // ── Galería de Fotos ──────────────────────────────────
                        if (order.photos.isNotEmpty()) {
                            item {
                                SectionHeader(icon = Icons.Default.Image, title = "GALERÍA DE FOTOS")
                            }
                            item {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(350))
                                ) {
                                    HorizontalPhotoStrip(
                                        photos = order.photos,
                                        onPhotoClick = { selectedPhotoUrl = it }
                                    )
                                }
                            }
                        }

                        // ── Servicios ─────────────────────────────────────────
                        item {
                            SectionHeader(icon = Icons.Default.Build, title = "SERVICIOS")
                        }
                        if (order.items.isEmpty()) {
                            item {
                                Text(
                                    "Sin servicios asignados",
                                    color = OnSurfaceVariantDark,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        } else {
                            items(order.items) { item ->
                                ServiceItemRow(item = item)
                                Spacer(Modifier.height(6.dp))
                            }
                        }

                        // ── Personal ──────────────────────────────────────────
                        item {
                            SectionHeader(icon = Icons.Default.Group, title = "PERSONAL ASIGNADO")
                        }
                        items(uiState.pendingStaff, key = { it.id }) { staffEntry ->
                            StaffRow(
                                staffEntry = staffEntry,
                                onRemove = { viewModel.removeStaff(staffEntry.id) },
                                modifier = Modifier.animateItem()
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        item {
                            AddStaffRow(onClick = { showStaffPicker = true })
                        }

                        // ── Estado ────────────────────────────────────────────
                        item {
                            SectionHeader(
                                icon = Icons.Default.CheckCircle,
                                title = "ACTUALIZAR ESTADO"
                            )
                        }
                        item {
                            StatusSelector(
                                orderStatus = order.status,
                                pendingStatus = uiState.selectedStatus,
                                onSelect = { viewModel.selectStatus(it) }
                            )
                        }

                        // ── Guardar ───────────────────────────────────────────
                        item { Spacer(Modifier.height(16.dp)) }
                        item {
                            Button(
                                onClick = { viewModel.saveChanges() },
                                enabled = !uiState.isSaving,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "Guardar Cambios",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                        item { Spacer(Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }

    // ── Selector de personal (bottom sheet) ──────────────────────────────────
    if (showStaffPicker) {
        val assignedIds = uiState.pendingStaff.mapNotNull { it.staffId }.toSet()
        val available = uiState.availableStaff.filter { it.id !in assignedIds }

        ModalBottomSheet(
            onDismissRequest = { showStaffPicker = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = SurfaceDark
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Seleccionar Personal",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (available.isEmpty()) {
                    Text(
                        "Todo el personal activo ya está asignado",
                        color = OnSurfaceVariantDark,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                } else {
                    available.forEach { member ->
                        StaffPickerRow(
                            member = member,
                            onClick = {
                                viewModel.addStaff(member)
                                showStaffPicker = false
                            }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // ── Visor de foto a pantalla completa ────────────────────────────────────
    selectedPhotoUrl?.let { url ->
        Dialog(
            onDismissRequest = { selectedPhotoUrl = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { selectedPhotoUrl = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "Foto de vehículo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth()
                )
                IconButton(
                    onClick = { selectedPhotoUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Composables privados ─────────────────────────────────────────────────────

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OnSurfaceVariantDark,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = title,
            color = OnSurfaceVariantDark,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun DetailCard(rows: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCardDark)
    ) {
        rows.forEachIndexed { index, (key, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(key, color = OnSurfaceVariantDark, fontSize = 14.sp)
                Text(
                    value,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                )
            }
            if (index < rows.size - 1) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun HorizontalPhotoStrip(photos: List<String>, onPhotoClick: (String) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        photos.forEach { url ->
            AsyncImage(
                model = url,
                contentDescription = "Foto de vehículo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPhotoClick(url) }
            )
        }

        println(photos)
        // Counter chip
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = OnSurfaceVariantDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${photos.size} FOTOS",
                    color = OnSurfaceVariantDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ServiceItemRow(item: OrderItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = null,
            tint = OnSurfaceVariantDark,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.serviceName, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            if (item.quantity > 1) {
                Text("x${item.quantity}", color = OnSurfaceVariantDark, fontSize = 12.sp)
            }
        }
        Text(
            "S/ ${"%.2f".format(item.subtotal)}",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun StaffRow(
    staffEntry: OrderStaff,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardDark)
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = OnSurfaceVariantDark,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(staffEntry.staffName, color = Color.White, fontSize = 14.sp)
            staffEntry.roleSnapshot?.let {
                Text(it.toDisplayName(), color = OnSurfaceVariantDark, fontSize = 12.sp)
            }
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Quitar",
                tint = OnSurfaceVariantDark,
                modifier = Modifier.size(16.dp)
            )
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
private fun StatusSelector(
    orderStatus: OrderStatus,
    pendingStatus: OrderStatus?,
    onSelect: (OrderStatus) -> Unit
) {
    val options = listOf(
        OrderStatus.EnProceso to "EN\nPROCESO",
        OrderStatus.Terminado to "TERMINADO",
        OrderStatus.Entregado to "ENTREGADO"
    )

    val nextValid: OrderStatus? = when (orderStatus) {
        OrderStatus.EnProceso -> OrderStatus.Terminado
        OrderStatus.Terminado -> OrderStatus.Entregado
        else -> null
    }

    val orderStatusIndex = options.indexOfFirst { it.first == orderStatus }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCardDark)
        ) {
            options.forEachIndexed { index, (status, label) ->
                val isPast = index < orderStatusIndex
                val isCurrent = status == orderStatus
                val isNextValid = status == nextValid
                val isUserSelected = status == pendingStatus
                val isBeyondNext = index > orderStatusIndex + 1
                val isClickable = isNextValid

                // The "active" display: white bg if it's what we'll end up as after save
                val showAsActive = isUserSelected || (isCurrent && pendingStatus == null)

                val bgColor by animateColorAsState(
                    targetValue = if (showAsActive) Color.White else Color.Transparent,
                    animationSpec = tween(200),
                    label = "status_bg_$index"
                )

                val textColor by animateColorAsState(
                    targetValue = when {
                        showAsActive -> BackgroundDark
                        isPast -> OnSurfaceVariantDark.copy(alpha = 0.45f)
                        isNextValid -> Color.White.copy(alpha = 0.75f)
                        isBeyondNext -> OnSurfaceVariantDark.copy(alpha = 0.25f)
                        else -> OnSurfaceVariantDark
                    },
                    animationSpec = tween(200),
                    label = "status_text_$index"
                )

                val shape = when (index) {
                    0 -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    options.size - 1 -> RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    else -> RoundedCornerShape(0.dp)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape)
                        .background(bgColor)
                        .then(if (isClickable) Modifier.clickable { onSelect(status) } else Modifier)
                        .padding(vertical = 13.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        when {
                            isPast -> Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = OnSurfaceVariantDark.copy(alpha = 0.45f),
                                modifier = Modifier.size(11.dp)
                            )
                            isBeyondNext -> Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = OnSurfaceVariantDark.copy(alpha = 0.25f),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Text(
                            text = label,
                            color = textColor,
                            fontSize = 9.sp,
                            fontWeight = if (showAsActive) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }

        // Hint below when next valid exists
        if (nextValid != null) {
            val hintText = if (pendingStatus != null)
                "El estado cambiará al guardar"
            else
                "Toca para avanzar al siguiente estado"
            Text(
                text = hintText,
                color = OnSurfaceVariantDark.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp, start = 2.dp)
            )
        } else {
            Text(
                text = "Estado final · no se puede modificar",
                color = OnSurfaceVariantDark.copy(alpha = 0.4f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp, start = 2.dp)
            )
        }
    }
}

@Composable
private fun StaffPickerRow(member: StaffMember, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceCardDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = OnSurfaceVariantDark,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(member.fullName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(member.role.toDisplayName(), color = OnSurfaceVariantDark, fontSize = 12.sp)
        }
    }
}

private fun StaffRole.toDisplayName(): String = when (this) {
    StaffRole.Admin -> "Administrador"
    StaffRole.Washer -> "Lavador"
    StaffRole.Cashier -> "Cajero"
    StaffRole.Supervisor -> "Supervisor"
}
