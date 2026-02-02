package com.example.csc481_bird_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.csc481_bird_app.ui.theme.Csc481birdappTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!hasCameraPermission()){
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), 0
            )//requestPermissions
            return
        }//if
        enableEdgeToEdge()
        setContent {
            Csc481birdappTheme {

            }//Theme
        }//setContent
    }//override fun

    //check that we have permission to use the camera in the first place
    private fun hasCameraPermission(): Boolean{
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }//Boolean fun
}//class