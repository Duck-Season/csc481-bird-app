package com.example.csc481_bird_app.utils

import android.content.Context
import android.util.Log
private val labelsFilename = "labels.txt"

fun getLabels (context: Context): List<String> {
    try {
        //attempt to load the labels file
        return context.assets.open(labelsFilename).bufferedReader().use { reader ->
            reader.readLines()
        }//open
    } catch (e: Exception){
        //something went wrong; give an emptyList
        Log.e("YOLODetector", "Error loading labels: ${e.message}")
        return emptyList()
    }//try-catch
}