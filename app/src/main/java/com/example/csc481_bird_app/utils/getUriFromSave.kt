package com.example.csc481_bird_app.utils

import android.content.Context
import android.net.Uri
import java.io.File

fun getImageUriFromSave(context: Context, saveName: String, saveDir: File? = null): Uri? {
    try {
        val saveFile = File(saveDir ?: context.filesDir, saveName)
        val reader = saveFile.bufferedReader()
        val uriString = reader.readLine()
        reader.close()
        return Uri.parse(uriString)
    } catch (e: Exception) {
        return null
    }//try-catch
}//fun

fun extractBirdNameFromSave(context: Context, saveName: String, saveDir: File? = null): String {
    try {
        val saveFile = File(saveDir ?: context.filesDir, saveName)
        val reader = saveFile.bufferedReader()
        reader.readLine() // Skip image URI
        reader.readLine() // Skip coordinates
        val firstDetectionLine = reader.readLine() ?: return "Unknown Bird"
        reader.close()

        // order when splitting should be:
        // [0-3] = RectF params, [4] = confidence Float, [5] = classIndex, [6] = subDetections, [7] = className
        val parts = firstDetectionLine.split("\t")
        if (parts.size >= 8) {
            return parts[7]
        }
        return "Unknown Bird"
    } catch (e: Exception) {
        return "Unknown Bird"
    }
}
