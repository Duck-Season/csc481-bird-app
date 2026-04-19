package com.example.csc481_bird_app.ui.screens.results

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap.createBitmap
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.csc481_bird_app.R
import com.example.csc481_bird_app.detector.DectectionsViewModel
import com.example.csc481_bird_app.filesaving.saveDetections
import com.example.csc481_bird_app.ui.ScanningIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    var isValidScan by remember { mutableStateOf(false) }

    //helper function to fetch location
    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        val lat = viewModel.geoLat
        val lon = viewModel.geoLon

        if (lat != null && lon != null) {
            scope.launch(Dispatchers.IO) {
                //attempt to fetch the location
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    //get the first address from coordinates
                    val addresses = geocoder.getFromLocation(lat.toDouble(), lon.toDouble(), 1)

                    //update the locationName
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

    //sharing function
    fun shareResults () {
        val uri = viewModel.bmpUri!!

        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }//val

            context.startActivity(
                Intent.createChooser(shareIntent, "Share Image")
            )//startActivity
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing image", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }//try-catch
    }//fun

    //------------ LaunchedEffects ------------

    //choose whether to enable image sharing
    LaunchedEffect(viewModel.detections, viewModel.bmpUri) {
        isValidScan = viewModel.detections.isNotEmpty() && viewModel.bmpUri != null
    }//LaunchedEffect

    //get geolocation on load-in
    LaunchedEffect(viewModel.geoLat, viewModel.geoLon) {
        fetchLocation()
    }//LaunchedEffect

    //------------ the Composable part ------------
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

                        Text("${viewModel.detections.size} bird${if(viewModel.detections.size == 1) "" else "s"} found")

                        Spacer(
                            modifier = Modifier.weight(1.5f)
                        )

                        //re-scan button
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

                        //sharing button
                        TextButton(
                            onClick = {
                                shareResults()
                            },
                            enabled = isValidScan
                        ) {
                            Row() {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_share_24),
                                    contentDescription = "Share the Results"
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
                        .weight(0.4f)
                    ) {
                        //display the image
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Image loaded from gallery to run detections on. Hopefully it's a bird of some sort.",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit     //don't stretch the image; keep aspect ratio
                        )//Image

                        //display the bounding boxes
                        BoundingBoxOverlay(viewModel, bmp, selectedIndex)
                    }//Box

                    Box(
                        modifier = Modifier
                            .weight(0.1f)
                            .height(32.dp)
                    ){
                        var displayStr = ""
                        if(locationName != null){
                            displayStr += locationName + " "
                        }//if

                        if(viewModel.geoLat != null && viewModel.geoLon != null){
                            displayStr += "(${viewModel.geoLat}, ${viewModel.geoLon})"
                        }//if

                        if(displayStr == ""){
                            displayStr = "Location Unknown"
                        }//if

                        Text(
                            text = "Taken at: \n$displayStr",
                            fontSize = 12.sp
                        )//Text
                    }//Box

                    LazyColumn(
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(0.3f),
                        contentPadding = PaddingValues(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if(viewModel.detections.isNotEmpty()){
                            itemsIndexed(viewModel.detections){ index, det ->
                                //------------ Detection Card ------------
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedIndex == index) {
                                            MaterialTheme.colorScheme.primaryFixedDim
                                        } else {
                                            MaterialTheme.colorScheme.primaryContainer
                                        }//if-else
                                    ),
                                    onClick = {
                                        if(selectedIndex == index){
                                            selectedIndex = -1
                                        }else{
                                            //set temporary selection values
                                            selectedIndex = index
                                        }//if-else
                                    }//onClick
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                    ) {
                                        Row() {
                                            //get width and height of bounding box; pick the smaller
                                            val bbW = det.bbox.right.toInt() - det.bbox.left.toInt()
                                            val bbH = det.bbox.bottom.toInt() - det.bbox.top.toInt()
                                            val bbS = minOf(bbW, bbH)

                                            //crop the image to the bounding box
                                            val previewBmp = createBitmap(
                                                bmp,
                                                det.bbox.left.toInt(),
                                                det.bbox.top.toInt(),
                                                bbS,
                                                bbS,
                                            )

                                            //a preview of the image is more intuitive than a date
                                            AsyncImage(
                                                model = previewBmp, // Get the URI/File instead of Bitmap
                                                contentDescription = "Preview",
                                                modifier = Modifier
                                                    .weight(0.2f)
                                                    .aspectRatio(1f / 1f)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop,
                                            )//AsyncImage

                                            Spacer(
                                                modifier = Modifier
                                                    .weight(0.1f)
                                            )//Spacer

                                            Text(
                                                text = "${index+1}.) " + det.className.substringAfter(" ").replace("_", " "),
                                                fontSize = 16.sp,
                                                modifier = Modifier
                                                    .weight(0.4f)
                                            )//Text

                                            Text(
                                                text = "${String.format("%.2f", det.confidence*100)}%",
                                                fontSize = 24.sp,
                                                modifier = Modifier
                                                    .weight(0.3f)
                                            )//Text
                                        }//Row

                                        if(selectedIndex == index){
                                            Text(
                                                text = "Other candidates",
                                                fontSize = 12.sp
                                            )//Text

                                            Column {
                                                det.subDetections.forEachIndexed { index, subDet ->
                                                    Column {
                                                        Row (
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                        ) {
                                                            Text(
                                                                text = subDet.first,
                                                                fontSize = 12.sp,
                                                                modifier = Modifier.weight(0.6f)
                                                            )
                                                            Text(
                                                                "${String.format("%.2f", subDet.second*100)}%",
                                                                fontSize = 12.sp,
                                                                modifier = Modifier.weight(0.4f)
                                                            )
                                                        }//Row
                                                        Row (
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                        )  {
                                                            LinearProgressIndicator(
                                                                progress = { subDet.second },
                                                            )
                                                        }//Row
                                                    }//Column
                                                }//forEach
                                            }//Column
                                        }//if
                                    }//Column
                                }//Card
                            }//itemsIndexed
                        } else {
                            //detections were not found
                            item{
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
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
                    //rescanning screen
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

                        ScanningIndicator(true)
                    }//Box
                }//if-else
            }//.let
        }//Column

        //confirm we want to do a rescan first
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