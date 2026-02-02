package com.example.csc481_bird_app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.csc481_bird_app.data.Detection
import com.example.csc481_bird_app.detector.YoloV11Detector

@Composable
fun ObjectDetectionScreen() {
    val context = LocalContext.current
    val detector = remember() { YoloV11Detector(context) }
    var detections by remember { mutableStateOf<List<Detection>>(emptyList()) }

    // Add camera preview and detection logic here
    // Process frames and update detections

    DisposableEffect(Unit) {
        onDispose {
            detector.close()
        }//dispose
    }//DisposableEffect
}//fun