package com.example.csc481_bird_app.ui.screens.results

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.csc481_bird_app.detector.DectectionsViewModel

@Composable
fun BoundingBoxOverlay(
    viewModel: DectectionsViewModel,
    bmp: Bitmap,
    selectedIndex: Int
){
    //drawing the bounding boxes
    Canvas(modifier = Modifier.fillMaxSize()) {
        //scale up and offset boxes relative to the image
        val scale = minOf(size.width / bmp.width, size.height / bmp.height)
        val offsetX = (size.width - bmp.width * scale) / 2f
        val offsetY = (size.height - bmp.height * scale) / 2f

        viewModel.detections.forEachIndexed { index, det ->
            //if a certain detection is selected, 'highlight' it on the image
            val bboxColor = if(index == selectedIndex){
                Color.Red
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
}//fun