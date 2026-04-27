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