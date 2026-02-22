package com.example.csc481_bird_app.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csc481_bird_app.R
import com.example.csc481_bird_app.detector.DectectionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: DectectionsViewModel,
    onBack: () -> Unit
){
    val context = LocalContext.current

    //mutable values
    var selectedIndex by remember { mutableStateOf<Int>(-1)}

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
                    }//Row
                }//title
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //only display if an image was successfully processed
            viewModel.bitmap?.let { bmp ->
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

                Box(
                    modifier = Modifier
                        .weight(0.1f)
                        .height(32.dp)
                ){
                    Text("Taken at: ${viewModel.geoLat}, ${viewModel.geoLon}")
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
            }//.let
        }//Column
    }//Scaffold
}//fun