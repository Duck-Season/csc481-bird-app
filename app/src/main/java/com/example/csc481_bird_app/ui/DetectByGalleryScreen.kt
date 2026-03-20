package com.example.csc481_bird_app.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import com.example.csc481_bird_app.R
import com.example.csc481_bird_app.detector.DectectionsViewModel
import com.example.csc481_bird_app.filesaving.saveDetections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//temporary Composable layout to test the model
//opens the phone's image gallery to load an image
@OptIn(ExperimentalMaterial3Api::class)
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
            viewModel.bmpUri = uri
            //launch on a separate thread so the app doesn't freeze
            scope.launch {
                // CRITICAL: Request permanent read access to this specific file
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: Exception) {
                    Log.e("csc481birdapp", "Failed to persist URI permission: ${e.message}")
                }//try-catch

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

                    //save results to a file
                    if(viewModel.detections.isNotEmpty()){
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val exif = ExifInterface(stream)
                            val coords = Pair(
                                exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)?.split("/")[0]?.toFloatOrNull(),
                                exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)?.split("/")[0]?.toFloatOrNull()
                            )//val Pair
                            saveDetections(
                                context,
                                viewModel.detections,
                                uri.toString(),
                                coords,
                                false
                            )
                        }//.use
                    }//if

                    //call function for moving into the results screen
                    onDetectionsComplete()
                }//.let
            }//scope launch
        }//.let
    }//val

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
                    ){
                        TextButton(
                            onClick = onBack,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.rounded_arrow_back_24),
                                contentDescription = "Back Arrow"
                            )//Icon
                        }//TextButton

                        Text("Pick Image From Gallery")
                    }//Row
                }//title
            )//TopAppBar
        }//topBar
    ) { innerPadding ->
        //Column to hold everything in place
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //button to open the image (gets disabled while processing)
            Button(
                onClick = { picker.launch("image/*") },
                enabled = !viewModel.isProcessing
            ) {
                Row() {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_add_photo_alternate_24),
                        contentDescription = "File Icon"
                    )//Icon
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if(viewModel.isProcessing) "Processing..." else "Select Image")
                }//Row
            }//Button

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
    }//Scaffold
}//fun