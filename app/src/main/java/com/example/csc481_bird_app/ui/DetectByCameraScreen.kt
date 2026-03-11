package com.example.csc481_bird_app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.csc481_bird_app.R
import com.example.csc481_bird_app.detector.DectectionsViewModel
import com.example.csc481_bird_app.filesaving.saveDetections
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectByCameraScreen(
    viewModel: DectectionsViewModel,
    onDetectionsComplete: () -> Unit,
    onBack: () -> Unit,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
){
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    //remember values
    var hasCameraPermission by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var hasTakenPicture by remember { mutableStateOf(false) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

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
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {context ->
                    val previewView = PreviewView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        this.scaleType = scaleType
                    }//val

                    val previewUseCase = Preview.Builder().build()
                    previewUseCase.setSurfaceProvider(previewView.surfaceProvider)

                    scope.launch{
                        val cameraProvider = context.cameraProvider()

                        try{
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, previewUseCase, imageCapture)
                        }catch (e: Exception) {
                            e.printStackTrace()
                        }//try-catch
                    }//.launch

                    previewView
                })//AndroidView

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

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        )
                    ){
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(64.dp)
                            )//CircularProgressIndicator

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Scanning for Birds...",
                                color = MaterialTheme.colorScheme.primary
                            )//Text
                        }//Column
                    }//Card
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

fun rotateBitmap(bitmap: Bitmap, uri: android.net.Uri, context: android.content.Context): Bitmap {
    val inputStream = context.contentResolver.openInputStream(uri)
    val exif = inputStream?.let { androidx.exifinterface.media.ExifInterface(it) }
    val orientation = exif?.getAttributeInt(
        androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
        androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
    )//val

    val matrix = android.graphics.Matrix()
    when (orientation) {
        androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        else -> return bitmap
    }//when

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}//fun