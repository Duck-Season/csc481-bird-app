package com.example.csc481_bird_app.ui

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
import androidx.exifinterface.media.ExifInterface
import com.example.csc481_bird_app.detector.DectectionsViewModel
import com.example.csc481_bird_app.filesaving.saveDetections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//temporary Composable layout to test the model
//opens the phone's image gallery to load an image
@Composable
fun DetectByGalleryScreen(
    viewModel: DectectionsViewModel,
    onDetectionsComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    //"rememberLauncherForActivityResult" loads a launcher for another app
    //"ActivityResultContracts" opens the file system
    //below the button passes in "image/*" to go straight to the image gallery
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        //".let" to only run when user actually picks something
        uri?.let {
            //launch on a separate thread so the app doesn't freeze
            scope.launch {
                //wrap in "withContext" to run on another thread and not freak the UI out
                val bitmap = withContext(Dispatchers.IO) {
                    //open the image file
                    context.contentResolver.openInputStream(uri)?.use {
                        //create bitmap from file (it *must* be a bitmap first)
                        BitmapFactory.decodeStream(it)
                    }//.use
                }//withContext

                //run detection when image is actually loaded as a bitmap
                bitmap?.let { bmp ->
                    viewModel.runDetections(bmp)

                    //save to a file if the path exists (which it should considering we already opened it)
                    val imgpath = uri.path
                    if(imgpath != null) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val exif = ExifInterface(stream)
                            val coords = Pair(
                                exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)?.split("/")[0]?.toFloat(),
                                exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)?.split("/")[0]?.toFloat()
                            )
                            saveDetections(context, viewModel.detections, imgpath, coords)
                        }//.use
                    }//if

                    onDetectionsComplete()
                }//.let
            }//scope launch
        }//.let
    }//val

    //Column to hold everything in place
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //button to open the image (gets disabled while processing)
        Button(
            onClick = { picker.launch("image/*") },
            enabled = !viewModel.isProcessing
        ) {
            Text(if(viewModel.isProcessing) "Processing..." else "Pick Image from Gallery")
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
                    contentDescription = "Image loaded from gallery to run detections on. Hopefully it's a bird of some sort.",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit     //don't stretch the image; keep aspect ratio
                )//Image
            }//Box
        }//.let
    }//Column
}//fun