package com.example.csc481_bird_app.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
import com.example.csc481_bird_app.detector.DectectionsViewModel

@Composable
fun ResultsScreen(
    viewModel: DectectionsViewModel,
    onBack: () -> Unit
){
    val context = LocalContext.current

    //Column to hold everything in place
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //only display if an image was successfully processed
        viewModel.bitmap?.let { bmp ->
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

                    viewModel.detections.forEach { det ->
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
                            Paint().apply {
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