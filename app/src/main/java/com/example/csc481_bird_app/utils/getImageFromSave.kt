package com.example.csc481_bird_app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.net.toUri

//get the path to the bitmap image from the scan file and load in the image as a bitmap
fun getImageFromSave (context: Context, scan_path: String): Bitmap? {
    try{
        //open the file chosen and read the first line
        //.use closes the stream automatically
        val iPath = context.openFileInput(scan_path).use(){ stream ->
            stream.bufferedReader().readLine()
        }//.use

        //decode the image into a bitmap and return it
        return context.contentResolver.openInputStream(iPath.toUri())?.use {
            BitmapFactory.decodeStream(it)
        }//.use
    } catch (e: Exception) {
        //give a null and an error log; null is to be handled by UI
        Log.e("csc481birdapp", "Error loading preview image: ${e.message}")
        return null
    }//try-catch
}//fun