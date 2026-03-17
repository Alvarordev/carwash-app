package com.example.carwash.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.carwash.domain.model.PaymentMethod
import com.example.carwash.ui.theme.OrangePrimary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryBottomSheet(
    paymentMethods: List<PaymentMethod>,
    isDelivering: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (paymentMethod: String, photoUris: List<Uri>) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current

    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraUri != null && photoUris.size < 4) {
            photoUris = photoUris + cameraUri!!
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && photoUris.size < 4) {
            photoUris = photoUris + uri
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "Confirmar Entrega",
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Text(
                "Método de pago",
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
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
                            containerColor = colorScheme.surfaceVariant,
                            labelColor = colorScheme.onSurfaceVariant
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

            Text(
                "Foto de entrega (opcional)",
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
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
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
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
                                    modifier = Modifier.size(20.dp).clip(CircleShape).background(colorScheme.scrim.copy(alpha = 0.7f)),
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
