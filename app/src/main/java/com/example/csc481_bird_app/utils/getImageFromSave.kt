package com.example.csc481_bird_app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File

//get the path to the bitmap image from the scan file and load in the image as a bitmap
fun getImageFromSave (context: Context, saveName: String, parentDir: File = context.filesDir): Bitmap? {
    try{
        //open the file chosen and read the first line
        val saveFile = File(parentDir, saveName)
        val uriString = saveFile.bufferedReader().use { it.readLine() }

        val uri = Uri.parse(uriString)
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }//use
    } catch (e: Exception) {
        //give a null and an error log; null is to be handled by UI
        Log.e("csc481birdapp", "Error loading image: ${e.message}")
        return null
    }//try-catch
}//fun