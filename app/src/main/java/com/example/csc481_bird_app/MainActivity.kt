package com.example.csc481_bird_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.csc481_bird_app.detector.DectectionsViewModel
import com.example.csc481_bird_app.ui.ChooseFileScreen
import com.example.csc481_bird_app.ui.DetectByCameraScreen
import com.example.csc481_bird_app.ui.DetectByGalleryScreen
import com.example.csc481_bird_app.ui.HomeScreen
import com.example.csc481_bird_app.ui.ResultsScreen
import com.example.csc481_bird_app.ui.ScanScreen
import com.example.csc481_bird_app.ui.theme.Csc481birdappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = DectectionsViewModel(application)

        enableEdgeToEdge()
        setContent {
            MaterialTheme() {
                //create controller for navigating screens
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onCameraClick = {
                                    navController.navigate("bycamera")
                                },
                                onGalleryClick = {
                                    navController.navigate("bygallery")
                                },
                                onFileClick = {
                                    navController.navigate("byfile")
                                }//onFileClick
                            )///HomeScreen
                        }//composable
                        composable("bycamera") {
                            ScanScreen(
                                onBack = { navController.navigate("home") }
                            )
                        }//composable
                        composable("byfile") {
                            ChooseFileScreen(
                                viewModel,
                                onDetectionsComplete = {
                                    navController.navigate("results")
                                },
                                onBack = {
                                    navController.navigate("home")
                                }//onBack
                            )//ChooseFileScreen
                        }//composable
                        composable("bygallery") {
                            DetectByGalleryScreen(
                                viewModel,
                                onDetectionsComplete = {
                                    navController.navigate("results")
                                },
                                onBack = {
                                    navController.navigate("home")
                                }//onBack
                            )//DetectByGalleryScreen
                        }//composable
                        composable("results") {
                            ResultsScreen(
                                viewModel,
                                onBack = {
                                    navController.navigate("home")
                                }//onBack
                            )//ResultsScreen
                        }//composable
                    }//NavHost
                }//Surface

            }//Theme
        }//setContent
    }//override fun

}//class