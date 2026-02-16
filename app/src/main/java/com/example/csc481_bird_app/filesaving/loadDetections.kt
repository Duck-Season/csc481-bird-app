package com.example.csc481_bird_app.filesaving

import android.content.Context
import android.graphics.RectF
import android.util.Log
import com.example.csc481_bird_app.detector.Detection
import java.io.BufferedReader
import java.io.InputStreamReader


//load in a list of detections from an internal file
//detections is self-explanatory
//scan_path is a path from the scan file in internal storage
fun loadDetections(context: Context, scan_path: String): SavedScan?{
    try{
        //open the file chosen
        val inputStream = context.openFileInput(scan_path)
        val reader = BufferedReader(InputStreamReader(inputStream))

        //get the image path
        val iPath = reader.readLine()

        //get the geoCoords
        val gList = reader.readLine()?.split(" ")
        val gCoords = Pair(gList?.get(0)?.toFloat(), gList?.get(1)?.toFloat())

        //then loop through detections
        val detList = mutableListOf<Detection>()
        var line = reader.readLine()
        while(line != null) {
            //everything in saveDetections() is split by a ' '
            //.readLine() doesn't include the '\n' so we don't need to worry about that
            val sList = line.split(" ")

            //make the entry
            detList.add(
                Detection(
                    bbox = RectF(sList[0].toFloat(), sList[1].toFloat(), sList[2].toFloat(), sList[3].toFloat(),),
                    confidence = sList[4].toFloat(),
                    classIndex = sList[5].toInt(),
                    className = sList.subList(6, sList.size).joinToString(" ") //class name likely has spaces in it
                )//new Detection
            )//.add

            //go to next line
            line = reader.readLine()
        }//while

        //close the input stream
        inputStream.close()

        //return list as an unmutable copy
        return SavedScan(iPath, gCoords, detList.toList())
    } catch (e: Exception) {
        //give a null and an error log; null is to be handled by UI
        Log.e("csc481birdapp", "Error loading detections: ${e.message}")
        return null
    }//try-catch
}//fun