package com.example.csc481_bird_app.filesaving

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.example.csc481_bird_app.detector.Detection
import java.util.Date

//save a list of detections to an internal file to be accessed later
//detections is self-explanatory
//coords are going to be passed either from geolocation API (camera mode) or EXIF data (gallery)
fun saveDetections(context: Context, detections: List<Detection>, imageUri: String?, coords: Pair<Float?, Float?>){
    //make sure that the list isn't empty first AND there's an image
    if(detections.isNotEmpty() && imageUri != null){
        try {
            //create filename for save
            //using Date to make unique-ish names
            //this might break some things elsewhere if somebody messes with the phone's clock settings
            //but idrc that much to foolproof it
            val saveName = "save_" + Date().time

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