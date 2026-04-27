package com.example.csc481_bird_app.filesaving

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import com.example.csc481_bird_app.detector.Detection
import java.util.Date

//save a list of detections to an internal file to be accessed later
//detections is self-explanatory
//coords are going to be passed either from geolocation API (camera mode) or EXIF data (gallery)
fun saveDetections(
    context: Context,
    detections: List<Detection>,
    imageUri: String?,
    coords: Pair<Float?, Float?>,
    isCameraSave: Boolean,
    prefs: SharedPreferences,
    forceSave :Boolean = false
){
    //make sure there's an image and either there's some detections or autosave for empty images is enabled
    val isAutosaveEnabled = prefs.getBoolean("pref_autosavingEnabled", true)
    val isAutosaveEmptyEnabled = prefs.getBoolean("pref_autosaveEmptyScans", false)
    if((((detections.isNotEmpty() && isAutosaveEnabled) || isAutosaveEmptyEnabled) || forceSave) && imageUri != null){
        try {
            //create filename for save
            //indicate whether camera or gallery was used
            //using Date to make unique-ish names
            var saveName = "save_" + (if(isCameraSave) "camera_" else "gallery_") + Date().time

            // If there's at least one detection, append the first bird's name for easier sorting/filtering
            // This allows the gallery screen to identify favorites without reading every file
            if (detections.isNotEmpty()) {
                val firstBird = detections.first().className
                    .substringAfter(" ") // remove potential index/prefix (e.g., "1.) ")
                    .substringBefore(" (") // remove "(Manual)" if present
                    .replace(" ", "_")
                saveName += "_$firstBird"
            }

            //start with the image path and geocoords first, we need only store them once
            var strContents = imageUri + "\n"
            strContents += coords.first.toString() + " " + coords.second.toString() + "\n"

            //then go through each detection
            detections.forEach { det ->
                //convert the entire detection to a string
                var strEntry = ""

                //order when splitting should be:
                // [0-3] = RectF params,
                // [4] = confidence Float,
                // [5] = classIndex,
                // [6] = subDetections,
                // [7] = className
                strEntry +=
                    det.bbox.left.toString() + "\t" +
                    det.bbox.top.toString() + "\t" +
                    det.bbox.right.toString() + "\t" +
                    det.bbox.bottom.toString() + "\t"
                strEntry += det.confidence.toString() + "\t"
                strEntry += det.classIndex.toString() + "\t"
                strEntry +=
                    det.subDetections[0].first + ":" + det.subDetections[0].second.toString() +
                    "|" + det.subDetections[1].first + ":" + det.subDetections[1].second.toString() + "\t"
                strEntry += det.className

                //add entry to contents
                strEntry += "\n"
                strContents += strEntry

                Log.d("csc481birdapp", "SAVING LINE: '$strEntry'")
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