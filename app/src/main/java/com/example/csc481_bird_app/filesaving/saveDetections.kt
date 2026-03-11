package com.example.csc481_bird_app.filesaving

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.example.csc481_bird_app.detector.Detection
import java.util.Date

//save a list of detections to an internal file to be accessed later
//detections is self-explanatory
//coords are going to be passed either from geolocation API (camera mode) or EXIF data (gallery)
fun saveDetections(context: Context, detections: List<Detection>, imageUri: String?, coords: Pair<Float?, Float?>, isCameraSave: Boolean){
    //make sure that the list isn't empty first AND there's an image
    if(detections.isNotEmpty() && imageUri != null){
        try {
            //create filename for save
            //indicate whether camera or gallery was used
            //using Date to make unique-ish names
            val saveName = "save_" + (if(isCameraSave) "camera_" else "gallery_") + Date().time

            //start with the image path and geocoords first, we need only store them once
            var strContents = imageUri + "\n"
            strContents += coords.first.toString() + " " + coords.second.toString() + "\n"

            //then go through each detection
            detections.forEach { det ->
                //convert the entire detection to a string
                var strEntry = ""

                //order when splitting should be: [0-3] = RectF params, [4] = confidence Float, [5] = classIndex, [6] = className
                strEntry += det.bbox.left.toString() + " " + det.bbox.top.toString() + " " + det.bbox.right.toString() + " " + det.bbox.bottom.toString() + " "
                strEntry += det.confidence.toString() + " "
                strEntry += det.classIndex.toString() + " "
                strEntry += det.className
                strEntry += "\n"

                //add entry to contents
                strContents += strEntry
            }//forEach

            //now save contents to the file
            val outputStream = context.openFileOutput(saveName, MODE_PRIVATE)
            outputStream.write(strContents.toByteArray())
            outputStream.close()
        } catch (e: Exception){
            //couldn't save properly
            Log.e("csc481birdapp", "Error saving detections: ${e.message}")
        }//try-catch
    }else{
        //give an error in Logcat
        Log.e("csc481birdapp", "Cannot save detections: Detections/Image not found")
    }//if-else
}//fun