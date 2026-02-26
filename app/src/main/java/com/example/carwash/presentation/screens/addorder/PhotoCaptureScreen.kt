package com.example.carwash.presentation.screens.addorder

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.carwash.presentation.navigation.Screen
import com.example.carwash.presentation.viewmodel.AddOrderViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PhotoCaptureScreen(navController: NavController, viewModel: AddOrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val cameraController = remember { LifecycleCameraController(context) }

    HandleCameraPermission(cameraPermissionState)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Paso 1: Añadir Fotos") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CameraSection(
                modifier = Modifier.weight(1f),
                isPermissionGranted = cameraPermissionState.status.isGranted,
                cameraController = cameraController
            )
            PhotoGallerySection(uiState.photos, viewModel)
            NavigationAndCaptureSection(
                navController = navController,
                viewModel = viewModel,
                cameraController = cameraController,
                isNextEnabled = uiState.photos.isNotEmpty()
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun HandleCameraPermission(cameraPermissionState: PermissionState) {
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
}

@Composable
private fun CameraSection(
    modifier: Modifier = Modifier,
    isPermissionGranted: Boolean,
    cameraController: LifecycleCameraController
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isPermissionGranted) {
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        this.controller = cameraController
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text("Se necesita permiso de cámara para continuar.")
        }
    }
}

@Composable
private fun PhotoGallerySection(photos: List<Uri>, viewModel: AddOrderViewModel) {
    PhotoGallery(
        photos = photos,
        onRemove = { viewModel.onPhotoRemoved(it) },
        onReorder = { from, to ->
            val mutableList = photos.toMutableList()
            val item = mutableList.removeAt(from)
            mutableList.add(to, item)
            viewModel.onPhotosReordered(mutableList)
        }
    )
}

@Composable
private fun NavigationAndCaptureSection(
    navController: NavController,
    viewModel: AddOrderViewModel,
    cameraController: LifecycleCameraController,
    isNextEnabled: Boolean
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CaptureButton(onCapture = {
            takePhoto(context, cameraController) { uri ->
                viewModel.onPhotoAdded(uri)
            }
        })
        Button(
            onClick = { navController.navigate(Screen.AddOrderVehicle.route) },
            enabled = isNextEnabled
        ) {
            Text("Siguiente")
        }
    }
}

@Composable
private fun CaptureButton(onCapture: () -> Unit) {
    IconButton(onClick = onCapture, modifier = Modifier.size(72.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White, CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
private fun PhotoGallery(
    photos: List<Uri>,
    onRemove: (Uri) -> Unit,
    onReorder: (Int, Int) -> Unit
) {
    if (photos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Añade hasta 4 fotos del vehículo.")
        }
        return
    }

    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(photos, key = { _, uri -> uri }) { index, uri ->
            var isBeingDragged by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { isBeingDragged = true },
                            onDragEnd = { isBeingDragged = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dragDistance = dragAmount.x
                                val currentLayoutInfo = listState.layoutInfo
                                val currentVisibleItems = currentLayoutInfo.visibleItemsInfo

                                if (isBeingDragged) {
                                    val currentItem = currentVisibleItems.firstOrNull { it.index == index }
                                    if (currentItem != null) {
                                        val itemCenter = currentItem.offset + currentItem.size / 2
                                        val targetItem = currentVisibleItems.find {
                                            if (it.index == index) return@find false
                                            val targetCenter = it.offset + it.size / 2
                                            if (dragDistance > 0) {
                                                itemCenter > targetCenter && itemCenter - dragDistance < targetCenter
                                            } else {
                                                itemCenter < targetCenter && itemCenter - dragDistance > targetCenter
                                            }
                                        }
                                        if (targetItem != null) {
                                            onReorder(index, targetItem.index)
                                        }
                                    }
                                }
                            }
                        )
                    }
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Foto del vehículo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { onRemove(uri) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Eliminar foto", tint = Color.White)
                }
            }
        }
    }
}

private fun takePhoto(context: Context, controller: CameraController, onPhotoTaken: (Uri) -> Unit) {
    val outputDirectory = context.filesDir
    val photoFile = File(
        outputDirectory,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    val executor: Executor = ContextCompat.getMainExecutor(context)

    (controller as LifecycleCameraController).takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                outputFileResults.savedUri?.let { onPhotoTaken(it) }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("PhotoCaptureScreen", "Error taking photo", exception)
            }
        }
    )
}