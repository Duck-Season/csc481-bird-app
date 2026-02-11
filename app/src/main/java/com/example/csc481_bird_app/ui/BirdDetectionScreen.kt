package com.example.csc481_bird_app.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//temporary Composable layout to test the model
@Composable
fun BirdDetectionScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    //"by remember {mutableStateOf<...>(...)}" is Compose's version of useState
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val detector = remember { YOLOv11Detector(context) }
    var detections by remember { mutableStateOf<List<Detection>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }

    //"rememberLauncherForActivityResult" loads a launcher for another app
    //"ActivityResultContracts" opens the file system
    //below the button passes in "image/*" to go straight to the image gallery
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        //".let" to only run when user actually picks something
        uri?.let {
            //launch on a separate thread so the app doesn't freeze
            scope.launch {
                //disable button until finished
                isProcessing = true

                //wrap in "withContext" to run on another thread and not freak the UI out
                bitmap = withContext(Dispatchers.IO) {
                    //open the image file
                    context.contentResolver.openInputStream(uri)?.use {
                        //create bitmap from file (it *must* be a bitmap first)
                        BitmapFactory.decodeStream(it)
                    }//.use
                }//withContext

                //run detection when image is actually loaded as a bitmap
                bitmap?.let { bmp ->
                    detections = withContext(Dispatchers.Default) {
                        //set threshold somewhat high but not too high
                        detector.detectObjects(bmp, 0.5f)
                    }//withContext
                }//.let

                //re-enable the button
                isProcessing = false
            }//scope launch
        }//.let
    }//val

    //need to close the detector when the composable is disposed
    DisposableEffect(Unit) {
        onDispose { detector.close() }
    }//DisposableEffect

    //Column to hold everything in place
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //button to open the image (gets disabled while processing)
        Button(
            onClick = { picker.launch("image/*") },
            enabled = !isProcessing
        ) {
            Text(if(isProcessing) "Processing..." else "Pick Image from Gallery")
        }//Button

        //only display if an image was successfully processed
        bitmap?.let { bmp ->
            //ok so unlike HTML/CSS Compose elements just stack on top by default
            Box(modifier = Modifier.fillMaxWidth()) {
                //display the image
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Image loaded from gallery to run detections on. Hopefully it's a bird of some sort.",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit     //don't stretch the image; keep aspect ratio
                )//Image

                //drawing the bounding boxes
                Canvas(modifier = Modifier.fillMaxSize()) {
                    //scale up and offset boxes relative to the image
                    val scale = minOf(size.width / bmp.width, size.height / bmp.height)
                    val offsetX = (size.width - bmp.width * scale) / 2f
                    val offsetY = (size.height - bmp.height * scale) / 2f

                    detections.forEach { det ->
                        //get bounding box dimensions from detection bbox
                        val left = det.bbox.left * scale + offsetX
                        val top = det.bbox.top * scale + offsetY
                        val width = det.bbox.width() * scale
                        val height = det.bbox.height() * scale

                        //draw the bounding box with stroke
                        drawRect(
                            color = Color.Green,
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            style = Stroke(width = 3f)
                        )//drawRect

                        //draw background for the label
                        drawRect(
                            color = Color.Green,
                            topLeft = Offset(left, top - 25f),
                            size = Size(width.coerceAtLeast(150f), 25f)
                        )//drawRect

                        //draw the text for the label
                        //need to call nativeCanvas
                        drawContext.canvas.nativeCanvas.drawText(
                            "${det.className} (${(det.confidence * 100).toInt()}% sure)",
                            left + 5f,
                            top - 8f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 14f
                            }//.apply
                        )//drawText
                    }//forEach
                }//Canvas
            }//Box
        }//.let
    }//Column
}//fun