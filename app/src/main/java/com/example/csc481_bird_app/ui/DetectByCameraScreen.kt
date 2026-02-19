package com.example.csc481_bird_app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.example.csc481_bird_app.detector.DectectionsViewModel
import com.example.csc481_bird_app.filesaving.saveDetections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

//temporary Composable layout to test the model
//opens the phone's camera to capture an image
@Composable
fun DetectByCameraScreen(
    viewModel: DectectionsViewModel,
    onDetectionsComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    //create temp file for full resolution camera image
    val imageUri = remember {
        val file = File(context.cacheDir, "camera_temp.jpg")
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    //"rememberLauncherForActivityResult" loads a launcher for another app
    //"ActivityResultContracts" opens the phone camera
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                //launch on a separate thread so the app doesn't freeze
                scope.launch {
                    //wrap in "withContext" to run on another thread and not freak the UI out
                    val bitmap = withContext(Dispatchers.IO) {
                        //open the image file
                        context.contentResolver.openInputStream(imageUri)?.use {
                            //create bitmap from file (it *must* be a bitmap first)
                            BitmapFactory.decodeStream(it)
                        }//.use
                    }//withContext

                    //run detection when image is actually loaded as a bitmap
                    bitmap?.let { bmp ->
                        viewModel.runDetections(bmp)

                        //save detections + GPS info
                        val imgpath = imageUri.path
                        if (imgpath != null) {
                            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                                val exif = ExifInterface(stream)
                                val coords = Pair(
                                    exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
                                        ?.split("/")?.firstOrNull()?.toFloat(),
                                    exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
                                        ?.split("/")?.firstOrNull()?.toFloat()
                                )
                                saveDetections(
                                    context,
                                    viewModel.detections,
                                    imgpath,
                                    coords
                                )
                            }//.use
                        }//if

                        onDetectionsComplete()
                    }//.let
                }//scope launch
            }
        }//launcher

    // Request CAMERA permission
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                cameraLauncher.launch(imageUri)
            }
        }

    //Column to hold everything in place
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        //button to open the camera (gets disabled while processing)
        Button(
            onClick = {
                if (
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    cameraLauncher.launch(imageUri)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            enabled = !viewModel.isProcessing
        ) {
            Text(if (viewModel.isProcessing) "Processing..." else "Open Camera")
        }//Button

        Button(onClick = onBack) {
            Text("Back Home")
        }

        //only display if an image was successfully processed
        viewModel.bitmap?.let { bmp ->
            Box(modifier = Modifier.fillMaxWidth()) {
                //display the image
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Image captured from camera to run detections on.",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit     //don't stretch the image; keep aspect ratio
                )//Image
            }//Box
        }//.let
    }//Column
}//fun