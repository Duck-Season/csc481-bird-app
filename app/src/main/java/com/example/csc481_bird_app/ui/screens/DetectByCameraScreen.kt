package com.example.csc481_bird_app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.csc481_bird_app.R
import com.example.csc481_bird_app.detector.DectectionsViewModel
import com.example.csc481_bird_app.filesaving.saveDetections
import com.example.csc481_bird_app.ui.ScanningIndicator
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectByCameraScreen(
    viewModel: DectectionsViewModel,
    onDetectionsComplete: () -> Unit,
    onBack: () -> Unit,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
){
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    //--------------- remember values ---------------
    //permission
    var hasCameraPermission by remember { mutableStateOf(false) }

    //camera building
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            this.scaleType = scaleType
        }//.apply
    }//remember

    //toggle which camera to use
    var useFrontCamera by remember { mutableStateOf(false) }

    //image capturing values
    val imageCapture = remember { ImageCapture.Builder().build() }
    var hasTakenPicture by remember { mutableStateOf(false) }

    //variables for displaying taps
    var tapPosition by remember {mutableStateOf<Offset?>(null)}
    var tapShowing by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    //--------------- Geolocation Service ---------------
    //set viewModel coords to the geolocation client's
    @SuppressLint("MissingPermission")
    fun getLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                viewModel.geoLat = it.latitude.toFloat()
                viewModel.geoLon = it.longitude.toFloat()
            }//.let
        }//.addOnSuccess
    }//fun

    //--------------- Camera Service ---------------
    fun captureImage(imageCapture: ImageCapture, cameraExecutor: Executor){
        //disable button
        hasTakenPicture = true

        //make sure the geocoordinates are obtained
        if (viewModel.geoLat == null || viewModel.geoLon == null) {
            getLocation()
            return
        }//if

        //create file in Pictures directory
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "bird_${Date().time}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BirdApp")
        }//val

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        //take the actual picture
        imageCapture.takePicture(
            outputFileOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: return

                    scope.launch {
                        val bitmap = withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(savedUri)?.use {
                                val decodedBmp = BitmapFactory.decodeStream(it)
                                val rotatedBmp = rotateBitmap(decodedBmp, savedUri, context)

                                context.contentResolver.openOutputStream(savedUri)?.use { outStream ->
                                    rotatedBmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                                }//.use

                                rotatedBmp
                            }//.use
                        }//val

                        bitmap?.let { bmp ->
                            viewModel.bitmap = bmp
                            viewModel.isProcessing = true
                            viewModel.runDetections(bmp)

                            val geoCoords = Pair(viewModel.geoLat, viewModel.geoLon)

                            if (viewModel.detections.isNotEmpty()) {
                                saveDetections(
                                    context,
                                    viewModel.detections,
                                    savedUri.toString(),
                                    geoCoords,
                                    true
                                )//saveDetections
                            }//if

                            onDetectionsComplete()
                        }//.let
                    }//.launch
                }//fun

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }//fun
            }//callback
        )//takePicture
    }//fun

    //--------------- Permissions ---------------
    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false

        hasCameraPermission = cameraGranted
        if (locationGranted) {
            getLocation()
        }//if
    }//val

    //--------------- LaunchedEffects ---------------

    //get permissions for the camera and location
    LaunchedEffect(Unit) {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )//val

        val needsPermissions = permissions.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }//val

        if (needsPermissions) {
            permissionsLauncher.launch(permissions)
        } else {
            hasCameraPermission = true
            getLocation()
        }//if-else
    }//LaunchedEffect

    //building up the camera preview
    LaunchedEffect(useFrontCamera) {
        val cameraProvider = context.cameraProvider()
        val previewUseCase = Preview.Builder().build()
        previewUseCase.setSurfaceProvider(previewView.surfaceProvider)

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
                else CameraSelector.DEFAULT_BACK_CAMERA,
                previewUseCase,
                imageCapture
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }//try-catch
    }//LaunchedEffect

    LaunchedEffect(tapPosition) {
        if (tapPosition != null) {
            tapShowing = true
            delay(600)
            tapShowing = false
        }//if
    }//LaunchedEffect

    //--------------- UI ---------------
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(id = R.drawable.rounded_arrow_back_24),
                                contentDescription = "Back Arrow"
                            )//Icon
                        }//TextButton
                        Text("Scan with Camera")
                    }//Row
                }//title
            )//TopAppBar
        }//topBar
    ) { innerPadding ->
        if(hasCameraPermission && !viewModel.isProcessing){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ){
                //where the camera feed appears
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize(),
                    factory = { previewView },
                    update = { view ->
                        //touch listener for tap-to-focus
                        view.setOnTouchListener { v, event ->
                            if (event.action == MotionEvent.ACTION_DOWN) {
                                val factory = view.meteringPointFactory
                                val point = factory.createPoint(event.x, event.y)
                                val action = FocusMeteringAction.Builder(
                                    point,
                                    FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                                ).setAutoCancelDuration(3, TimeUnit.SECONDS)
                                    .build()
                                camera?.cameraControl?.startFocusAndMetering(action)

                                //set pointer
                                tapPosition = Offset(event.x, event.y)

                                v.performClick()
                            }//if
                            true
                        }//.setOnTouch
                    }//update
                )//AndroidView

                //animated tap pointer
                tapPosition?.let { position ->
                    //it's the alpha AND scale!
                    val alphaScale by animateFloatAsState(
                        targetValue = if (tapShowing) 1f else 0f,
                        animationSpec = tween(durationMillis = 300),
                    )//val

                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        drawCircle(
                            color = Color.White.copy(alpha = alphaScale),
                            radius = 32.dp.toPx() * alphaScale,
                            center = position,
                        )//drawCircle
                    }//Canvas
                }//.let

                //switch cameras
                IconButton(
                    onClick = { useFrontCamera = !useFrontCamera },
                    enabled = !hasTakenPicture, //disable the flip while capturing
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.outline_cameraswitch_24),
                        contentDescription = "Capture Image",
                        modifier = Modifier
                            .size(256.dp),
                        colorFilter = if(hasTakenPicture){ColorFilter.tint(Color.Gray) } else { ColorFilter.tint(Color.White)}
                    )//Image
                }//IconButton

                //button to take the picture
                IconButton(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    onClick = {
                        captureImage(imageCapture, cameraExecutor)
                    },
                    enabled = !hasTakenPicture
                ) {
                    Image(
                        painter = painterResource(R.drawable.baseline_circle_24),
                        contentDescription = "Capture Image",
                        modifier = Modifier
                            .size(256.dp),
                        colorFilter = if(hasTakenPicture){ColorFilter.tint(Color.Gray) } else { ColorFilter.tint(Color.White)}
                    )//Image
                }//IconButton
            }//Box
        }else{
            if(viewModel.isProcessing && viewModel.bitmap != null){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = viewModel.bitmap!!.asImageBitmap(),
                        contentDescription = "Processing Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        alpha = 0.5f
                    )//Image

                    ScanningIndicator(false)
                }//Box
            }else{
                Card(
                    modifier = Modifier
                        .padding(innerPadding)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_close_24),
                            contentDescription = "Back Arrow"
                        )//Icon
                        Text(
                            text = "Camera Disabled",
                            fontSize = 30.sp
                        )//Text
                        Text("Please enable camera permissions in the app settings.")
                    } //Column
                }//Card
            }//if-else
        }//if-else
    }//Scaffold
}//fun

suspend fun Context.cameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    val listenableFuture = ProcessCameraProvider.getInstance(this)
    listenableFuture.addListener({
        continuation.resume(listenableFuture.get())
    }, ContextCompat.getMainExecutor(this))
}//suspend fun

fun rotateBitmap(bitmap: Bitmap, uri: Uri, context: Context): Bitmap {
    val inputStream = context.contentResolver.openInputStream(uri)
    val exif = inputStream?.let { ExifInterface(it) }
    val orientation = exif?.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )//val

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        else -> return bitmap
    }//when

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}//fun