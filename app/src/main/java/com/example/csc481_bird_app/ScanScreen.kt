package com.example.csc481_bird_app

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun ScanScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var detections by remember { mutableStateOf<List<Detection>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    var gpsCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    var showCamera by remember { mutableStateOf(false) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Get GPS location
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
    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bmp ->
            bmp?.let {
                //launch on a separate thread so the app doesn't freeze
                scope.launch {



                    //disable button until finished
                    isProcessing = true


                    //wrap in "withContext" to run on another thread and not freak the UI out
                    bitmap = withContext(Dispatchers.IO) {
                        it
                    }//withContext

                    //run detection when image is actually loaded as a bitmap
                    bitmap?.let { bmp ->
                        detections = withContext(Dispatchers.Default) {
                            //set threshold somewhat high but not too high
                            detector.detectObjects(bmp, 0.5f)
                        }//withContext
                    }//.let

                    isProcessing = false
                }
            }
        }
    // Request CAMERA permission
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasPermission = granted
            if (granted) showCamera = true
        }

    if (!showCamera) {
        // Button screen
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                if (hasPermission) {
                    showCamera = true
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }) {
                Text("Open Camera")
            }
        }
    } else {
        // Camera preview screen
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val controller = LifecycleCameraController(ctx)

                controller.bindToLifecycle(lifecycleOwner)
                previewView.controller = controller

                previewView
            }
        )
    }
}