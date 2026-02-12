package com.example.csc481_bird_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.csc481_bird_app.ui.BirdDetectionScreen
import com.example.csc481_bird_app.ui.theme.Csc481birdappTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Csc481birdappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onScanClick = { navController.navigate("scan") },
                                onUploadClick = { navController.navigate("birdDetection") }
                            )
                        }
                        composable("scan") {
                            ScanScreen(onBack = { navController.popBackStack() })
                        }
                        composable("birdDetection") {
                            BirdDetectionScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }//NavHost
                }//Surface
            }//Theme
        }//setContent
    }//override fun
}//class