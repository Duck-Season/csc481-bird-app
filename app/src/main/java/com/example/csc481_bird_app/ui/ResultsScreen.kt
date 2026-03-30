package com.example.csc481_bird_app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.example.csc481_bird_app.R
import com.example.csc481_bird_app.detector.DectectionsViewModel
import com.example.csc481_bird_app.filesaving.saveDetections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: DectectionsViewModel,
    onBack: () -> Unit
){
    val context = LocalContext.current

    //mutable values
    var selectedIndex by remember { mutableStateOf<Int>(-1)}
    var locationName by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var showRescanDialog by remember { mutableStateOf(false) }

    var allBirds by remember { mutableStateOf(listOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedBird by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    fun openBirdPage(birdName: String) {
        val urlName = birdName.replace(" ", "_") // format for AllAboutBirds
        val url = "https://www.allaboutbirds.org/guide/$urlName"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    // Helper function to fetch location (DRY - Don't Repeat Yourself)
    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        val lat = viewModel.geoLat
        val lon = viewModel.geoLon

        if (lat != null && lon != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    // 3. Use the local variables instead of the viewModel properties
                    val addresses = geocoder.getFromLocation(lat.toDouble(), lon.toDouble(), 1)

                    if (!addresses.isNullOrEmpty()) {
                        val city = addresses[0].locality ?: ""
                        val state = addresses[0].adminArea ?: ""
                        withContext(Dispatchers.Main) {
                            locationName = "$city, $state"
                        }//withContext
                    }//if
                } catch (e: Exception) {
                    e.printStackTrace()
                }//try-catch
            }//.launch
        }//if
    }//fun

    LaunchedEffect(viewModel.geoLat, viewModel.geoLon) {
        fetchLocation()
    }//LaunchedEffect

    LaunchedEffect(Unit) {
        val input = context.assets.open("labels.txt")
        val list = input.bufferedReader().readLines().map {
            it.substringAfter(" ").replace("_", " ")
        }
        allBirds = list
    }

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

                        Text("Results Screen")

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = {
                                //display the dialog for deleting files
                                showRescanDialog = true
                            }//onClick
                        ) {
                            Row() {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_rescan_24),
                                    contentDescription = "Rescan Selected File"
                                )//Icon
                            }//Row
                        }//Button
                    }//Row
                }//title
            )//TopAppBar
        }//Scaffold
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //only display if an image was successfully processed
            viewModel.bitmap?.let { bmp ->
                if(!viewModel.isProcessing){
                    //ok so unlike HTML/CSS Compose elements just stack on top by default
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
                    ) {
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

                            viewModel.detections.forEachIndexed { index, det ->
                                //if a certain detection is selected, 'highlight' it on the image
                                val bboxColor = if(index == selectedIndex){
                                    Color.Blue
                                }else{
                                    Color.Green
                                }//if-else

                                //get bounding box dimensions from detection bbox
                                val left = det.bbox.left * scale + offsetX
                                val top = det.bbox.top * scale + offsetY
                                val width = det.bbox.width() * scale
                                val height = det.bbox.height() * scale

                                //draw the bounding box with stroke
                                drawRect(
                                    color = bboxColor,
                                    topLeft = Offset(left, top),
                                    size = Size(width, height),
                                    style = Stroke(width = 3f)
                                )//drawRect

                                //draw background for the label
                                drawRect(
                                    color = bboxColor,
                                    topLeft = Offset(left, top - 25f),
                                    size = Size(width.coerceAtLeast(150f), 25f)
                                )//drawRect

                                //draw the text for the label
                                //need to call nativeCanvas
                                drawContext.canvas.nativeCanvas.drawText(
                                    "${det.className} (${(det.confidence * 100).toInt()}% sure)",
                                    left + 5f,
                                    top - 8f,
                                    Paint().apply {
                                        color = android.graphics.Color.BLACK
                                        textSize = 14f
                                    }//.apply
                                )//drawText
                            }//forEach
                        }//Canvas
                    }//Box

                    // ---- Add Share Button ----
                    IconButton(
                        onClick = {
                            val uri = viewModel.bmpUri

                            if (uri != null) {
                                try {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "image/*"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                                    }

                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "Share Image")
                                    )

                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error sharing image", Toast.LENGTH_SHORT).show()
                                    e.printStackTrace()
                                }
                            } else {
                                Toast.makeText(context, "No image to share", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                            .size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.Blue
                        )
                    }

                    if(viewModel.geoLat != null && viewModel.geoLon != null) {
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(
                                LatLng(viewModel.geoLat!!.toDouble(), viewModel.geoLon!!.toDouble()),
                                15f // zoom level
                            )
                        }

                        GoogleMap(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(16.dp),
                            cameraPositionState = cameraPositionState
                        ) {
                            Marker(
                                state = MarkerState(
                                    position = LatLng(viewModel.geoLat!!.toDouble(), viewModel.geoLon!!.toDouble())
                                ),
                                title = locationName ?: "Unknown Location"
                            )
                        }
                    } else {
                        Text(
                            text = "Location not available",
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(0.3f),
                        contentPadding = PaddingValues(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if(viewModel.detections.isNotEmpty()){
                            itemsIndexed(viewModel.detections){ index, det ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "${index+1}.) " + det.className.substringAfter(" ").replace("_", " "),
                                            fontSize = 16.sp,
                                            modifier = Modifier
                                                .weight(0.6f)
                                        )//Text

                                        Text(
                                            text = "${String.format("%.2f", det.confidence*100)}%",
                                            fontSize = 24.sp,
                                            modifier = Modifier
                                                .weight(0.4f)
                                        )//Text
                                    }//Row
                                }//Card
                            }//itemsIndexed
                        } else {
                            item{
                                Card() {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "No detections found...",
                                            fontSize = 30.sp
                                        )//Text
                                    } //Column
                                }//Card
                            }//item
                        }//if-else
                    }//LazyColumn
                }else{
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
                                    text = "Rescanning for birds...",
                                    color = MaterialTheme.colorScheme.primary
                                )//Text
                            }//Column
                        }//Card
                    }//Box
                }//if-else
            }//.let
        }//Column

        if(showRescanDialog){
            AlertDialog(
                icon = {
                    Icon(painter = painterResource(id = R.drawable.outline_rescan_24), contentDescription = "Rescan Current Results")
                },
                title = {
                    Text("Rescan")
                },
                text = {
                    Text("Would you like to rescan the current results? This will create another save file.")
                },
                onDismissRequest = {
                    showRescanDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                //hide dialog
                                showRescanDialog = false

                                //run detections again
                                viewModel.runDetections(viewModel.bitmap!!)

                                //save to file
                                saveDetections(
                                    context,
                                    viewModel.detections,
                                    viewModel.bmpUri.toString(),
                                    Pair(viewModel.geoLat, viewModel.geoLon),
                                    viewModel.takenWithCamera
                                )//saveDetections
                            }//scope.launch
                        }//onClick
                    ) {
                        Text("Yes")
                    }//TextButton
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showRescanDialog = false
                        }//onClick
                    ) {
                        Text("No")
                    }//TextButton
                }//dismissButton
            )//AlertDialog
        }//if
    }//Scaffold
}//fun