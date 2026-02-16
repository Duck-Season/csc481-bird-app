package com.example.csc481_bird_app.detector

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//view model for running model detections on image
//necessary for moving data between screens (i.e. Camera/Gallery/File -> Results screen)
class DectectionsViewModel(application: Application) : AndroidViewModel(application) {
    var bitmap by mutableStateOf<Bitmap?>(null)
    var detections by mutableStateOf<List<Detection>>(emptyList())
    var isProcessing by mutableStateOf(false)

    //create detector model instance with application context
    private val detector = YOLOv11Detector(application)

    suspend fun runDetections(newBitmap: Bitmap) {

        //can disable certain UI elements while processing
        isProcessing = true

        //set current bitmap image
        bitmap = newBitmap

        //run detection on the bitmap
        detections = withContext(Dispatchers.Default) {
            detector.detectObjects(newBitmap, 0.25f)
        }//withContext

        //enable UI elements again
        isProcessing = false
    }//fun

    //close the detector when the app closes
    override fun onCleared() {
        detector.close()
    }//fun
}//class