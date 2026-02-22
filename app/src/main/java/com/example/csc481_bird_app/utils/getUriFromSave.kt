package com.example.csc481_bird_app.utils

import android.content.Context
import android.net.Uri

fun getImageUriFromSave(context: Context, saveName: String): Uri? {
    try {
        val reader = context.openFileInput(saveName).bufferedReader()
        val uriString = reader.readLine()
        reader.close()
        return Uri.parse(uriString)
    } catch (e: Exception) {
        return null
    }//try-catch
}//fun