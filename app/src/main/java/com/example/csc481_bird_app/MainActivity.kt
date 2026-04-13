package com.example.csc481_bird_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.csc481_bird_app.detector.DectectionsViewModel
import com.example.csc481_bird_app.ui.screens.choosefile.ChooseFileScreen
import com.example.csc481_bird_app.ui.screens.DetectByCameraScreen
import com.example.csc481_bird_app.ui.screens.DetectByGalleryScreen
import com.example.csc481_bird_app.ui.screens.HomeScreen
import com.example.csc481_bird_app.ui.screens.results.ResultsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = DectectionsViewModel(application)

        enableEdgeToEdge()
        setContent {
            MaterialTheme() {
                //create controller for navigating screens
                val navController = rememberNavController()

                fun onBackToHome(){
                    //reset everything in the viewModel
                    viewModel.isProcessing = false;
                    viewModel.bitmap = null;
                    viewModel.detections = emptyList();
                    viewModel.geoLat = null;
                    viewModel.geoLon = null;
                    viewModel.takenWithCamera = false;
                    viewModel.bmpUri = null;

                    //move back to home screen
                    navController.navigate("home")
                }//fun

                val imageCapture = remember {ImageCapture.Builder().build()}

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "bycamera") {
                        composable("home") {
                            HomeScreen(
                                onCameraClick = {
                                    viewModel.takenWithCamera = true;
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
                            // Ensure camera state is set correctly when starting here
                            viewModel.takenWithCamera = true
                            DetectByCameraScreen(
                                viewModel,
                                onDetectionsComplete = {
                                    navController.navigate("results")
                                },
                                onBack = {
                                    onBackToHome()
                                }//onBack
                            )//DetectByCameraScreen
                        }//composable
                        composable("bygallery") {
                            DetectByGalleryScreen(
                                viewModel,
                                onDetectionsComplete = {
                                    navController.navigate("results")
                                },
                                onBack = {
                                    onBackToHome()
                                }//onBack
                            )//DetectByGalleryScreen
                        }//composable
                        composable("byfile") {
                            ChooseFileScreen(
                                viewModel,
                                onDetectionsComplete = {
                                    navController.navigate("results")
                                },
                                onBack = {
                                    onBackToHome()
                                }//onBack
                            )//ChooseFileScreen
                        }//composable
                        composable("results") {
                            ResultsScreen(
                                viewModel,
                                onBack = {
                                    onBackToHome()
                                }//onBack
                            )//ResultsScreen
                        }//composable
                    }//NavHost
                }//Surface
            }//Theme
        }//setContent
    }//override fun
}//class