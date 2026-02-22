package com.example.csc481_bird_app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log

//get the path to the bitmap image from the scan file and load in the image as a bitmap
fun getImageFromSave (context: Context, saveName: String): Bitmap? {
    try{
        //open the file chosen and read the first line
        val reader = context.openFileInput(saveName).bufferedReader()
        val uriString = reader.readLine()
        reader.close()

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