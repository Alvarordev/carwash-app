package com.example.carwash.presentation.screens.addorder

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.carwash.presentation.navigation.Screen
import com.example.carwash.presentation.viewmodel.AddOrderViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PhotoCaptureScreen(navController: NavController, viewModel: AddOrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    // Request permission on first launch
    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    // Hold camera references
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) { onDispose { cameraExecutor.shutdown() } }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Fotos del vehículo (${uiState.photos.size}/4)") },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color.Black,
                                        titleContentColor = Color.White
                                )
                )
            },
            containerColor = Color.Black
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // ---- Camera Preview ----
            Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
            ) {
                if (cameraPermission.status.isGranted) {
                    AndroidView(
                            factory = { ctx ->
                                val previewView =
                                        PreviewView(ctx).apply {
                                            implementationMode =
                                                    PreviewView.ImplementationMode.COMPATIBLE
                                            scaleType = PreviewView.ScaleType.FILL_CENTER
                                        }

                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener(
                                        {
                                            val cameraProvider = cameraProviderFuture.get()

                                            val preview =
                                                    Preview.Builder().build().also {
                                                        it.surfaceProvider =
                                                                previewView.surfaceProvider
                                                    }

                                            val capture =
                                                    ImageCapture.Builder()
                                                            .setCaptureMode(
                                                                    ImageCapture
                                                                            .CAPTURE_MODE_MINIMIZE_LATENCY
                                                            )
                                                            .build()
                                            imageCapture = capture

                                            try {
                                                cameraProvider.unbindAll()
                                                cameraProvider.bindToLifecycle(
                                                        lifecycleOwner,
                                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                                        preview,
                                                        capture
                                                )
                                            } catch (e: Exception) {
                                                Log.e("PhotoCapture", "Camera binding failed", e)
                                            }
                                        },
                                        ContextCompat.getMainExecutor(ctx)
                                )

                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                    text = "Se necesita permiso de cámara.",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // ---- Photo Gallery ----
            PhotoGallery(
                    photos = uiState.photos,
                    onRemove = { viewModel.onPhotoRemoved(it) },
                    onReorder = { from, to ->
                        val list = uiState.photos.toMutableList()
                        val item = list.removeAt(from)
                        list.add(to, item)
                        viewModel.onPhotosReordered(list)
                    }
            )

            // ---- Controls: Capture + Next ----
            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(Color.Black)
                                    .padding(horizontal = 32.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // Spacer on left for balance
                Box(modifier = Modifier.size(48.dp))

                // Shutter button
                IconButton(
                        onClick = {
                            if (uiState.photos.size < 4) {
                                takePhoto(context, imageCapture, cameraExecutor) { uri ->
                                    viewModel.onPhotoAdded(uri)
                                }
                            }
                        },
                        modifier = Modifier.size(72.dp),
                        enabled = uiState.photos.size < 4
                ) {
                    Box(
                            modifier =
                                    Modifier.size(72.dp)
                                            .background(Color.White, CircleShape)
                                            .padding(4.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .background(
                                                        if (uiState.photos.size < 4) Color.White
                                                        else Color.Gray,
                                                        CircleShape
                                                )
                        )
                    }
                }

                // Next button
                Button(
                        onClick = { navController.navigate(Screen.AddOrderVehicle.route) },
                        enabled = uiState.photos.isNotEmpty()
                ) {
                    Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Siguiente")
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoGallery(
        photos: List<Uri>,
        onRemove: (Uri) -> Unit,
        onReorder: (Int, Int) -> Unit
) {
    val listState = rememberLazyListState()

    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.85f))
                            .height(if (photos.isEmpty()) 80.dp else 120.dp),
            contentAlignment = Alignment.Center
    ) {
        if (photos.isEmpty()) {
            Text(
                    "Toma al menos 1 foto (máx. 4)",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(photos, key = { _, uri -> uri.toString() }) { index, uri ->
                    var isBeingDragged by remember { mutableStateOf(false) }

                    Box(
                            modifier =
                                    Modifier.size(100.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .pointerInput(index) {
                                                detectDragGesturesAfterLongPress(
                                                        onDragStart = { isBeingDragged = true },
                                                        onDragEnd = { isBeingDragged = false },
                                                        onDragCancel = { isBeingDragged = false },
                                                        onDrag = { change, dragAmount ->
                                                            change.consume()
                                                            if (isBeingDragged) {
                                                                val visibleItems =
                                                                        listState
                                                                                .layoutInfo
                                                                                .visibleItemsInfo
                                                                val currentItem =
                                                                        visibleItems.firstOrNull {
                                                                            it.index == index
                                                                        }
                                                                if (currentItem != null) {
                                                                    val itemCenter =
                                                                            currentItem.offset +
                                                                                    currentItem
                                                                                            .size /
                                                                                            2
                                                                    val targetItem =
                                                                            visibleItems.find {
                                                                                if (it.index ==
                                                                                                index
                                                                                )
                                                                                        return@find false
                                                                                val tCenter =
                                                                                        it.offset +
                                                                                                it.size /
                                                                                                        2
                                                                                if (dragAmount.x > 0
                                                                                )
                                                                                        itemCenter >
                                                                                                tCenter &&
                                                                                                itemCenter -
                                                                                                        dragAmount
                                                                                                                .x <
                                                                                                        tCenter
                                                                                else
                                                                                        itemCenter <
                                                                                                tCenter &&
                                                                                                itemCenter -
                                                                                                        dragAmount
                                                                                                                .x >
                                                                                                        tCenter
                                                                            }
                                                                    if (targetItem != null)
                                                                            onReorder(
                                                                                    index,
                                                                                    targetItem.index
                                                                            )
                                                                }
                                                            }
                                                        }
                                                )
                                            }
                    ) {
                        AsyncImage(
                                model = uri,
                                contentDescription = "Foto ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                        )

                        // Index badge
                        Box(
                                modifier =
                                        Modifier.align(Alignment.BottomStart)
                                                .padding(4.dp)
                                                .background(
                                                        Color.Black.copy(alpha = 0.6f),
                                                        CircleShape
                                                )
                                                .size(20.dp),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                    "${index + 1}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                            )
                        }

                        // Delete button
                        IconButton(
                                onClick = { onRemove(uri) },
                                modifier =
                                        Modifier.align(Alignment.TopEnd)
                                                .padding(2.dp)
                                                .size(28.dp)
                                                .background(
                                                        Color.Black.copy(alpha = 0.6f),
                                                        CircleShape
                                                )
                        ) {
                            Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun takePhoto(
        context: Context,
        imageCapture: ImageCapture?,
        executor: ExecutorService,
        onPhotoTaken: (Uri) -> Unit
) {
    val capture =
            imageCapture
                    ?: run {
                        Log.w("PhotoCapture", "ImageCapture not ready yet")
                        return
                    }

    val photoFile =
            File(
                    context.filesDir,
                    "photo_${SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(System.currentTimeMillis())}.jpg"
            )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    capture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    onPhotoTaken(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("PhotoCapture", "Error taking photo: ${exception.message}", exception)
                }
            }
    )
}
