package com.example.csc481_bird_app.ui

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.csc481_bird_app.detector.Detection
import com.example.csc481_bird_app.detector.YOLOv11Detector
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ScanScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var detections by remember { mutableStateOf<List<Detection>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    var gpsCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    val detector = remember { YOLOv11Detector(context) }
    val fusedLocationClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            gpsCoords = it.latitude to it.longitude
                        }
                    }
            }
        }

    LaunchedEffect(Unit) {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        bmp?.let {
            scope.launch {
                isProcessing = true



                bitmap = withContext(Dispatchers.IO) { it }

                bitmap?.let { bmp ->
                    detections = withContext(Dispatchers.Default) {
                        detector.detectObjects(bmp, 0.5f)
                    }
                }

                isProcessing = false
            }
        }
    }

    // Helper function to fetch location (DRY - Don't Repeat Yourself)
    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                gpsCoords = it.latitude to it.longitude
                scope.launch(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val city = addresses[0].locality ?: ""
                            val state = addresses[0].adminArea ?: ""
                            withContext(Dispatchers.Main) {
                                locationName = "$city, $state"
                            }
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        }
    }

    // Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchLocation()
    }

    // Request CAMERA permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        }
    }

    DisposableEffect(Unit) {
        onDispose { detector.close() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                if (
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    cameraLauncher.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            enabled = !isProcessing
        ) {
            Text(if (isProcessing) "Processing..." else "Open Camera")
        }

        Button(onClick = onBack) {
            Text("Back Home")
        }

        bitmap?.let { bmp ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(bmp.width.toFloat() / bmp.height.toFloat())
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Captured image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scale =
                        minOf(size.width / bmp.width, size.height / bmp.height)
                    val offsetX =
                        (size.width - bmp.width * scale) / 2f
                    val offsetY =
                        (size.height - bmp.height * scale) / 2f

                    detections.forEach { det ->
                        val left = det.bbox.left * scale + offsetX
                        val top = det.bbox.top * scale + offsetY
                        val width = det.bbox.width() * scale
                        val height = det.bbox.height() * scale

                        drawRect(
                            color = Color.Green,
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            style = Stroke(width = 3f)
                        )

                        drawRect(
                            color = Color.Green,
                            topLeft = Offset(left, top - 25f),
                            size = Size(width.coerceAtLeast(150f), 25f)
                        )

                        drawContext.canvas.nativeCanvas.drawText(
                            "${det.className} (${(det.confidence * 100).toInt()}% sure)",
                            left + 5f,
                            top - 8f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 14f
                            }
                        )
                    }
                }
            }

            gpsCoords?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text("📍 Location: ${it.first}, ${it.second}")
            }
        }
    }
}
