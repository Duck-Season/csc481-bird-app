package com.example.csc481_bird_app

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.csc481_bird_app.detector.DectectionsViewModel
import com.example.csc481_bird_app.ui.screens.choosefile.ChooseFileScreen
import com.example.csc481_bird_app.ui.screens.DetectByCameraScreen
import com.example.csc481_bird_app.ui.screens.DetectByGalleryScreen
import com.example.csc481_bird_app.ui.screens.FAQScreen
import com.example.csc481_bird_app.ui.screens.HomeScreen
import com.example.csc481_bird_app.ui.screens.SettingsScreen
import com.example.csc481_bird_app.ui.screens.results.ResultsScreen
import com.example.csc481_bird_app.ui.theme.Csc481birdappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = DectectionsViewModel(application)

        //needed for managing preferences
        val sharedPrefs = getPreferences(MODE_PRIVATE)
        var themePref by mutableStateOf(sharedPrefs.getString("pref_systemTheme", "Light") ?: "Light")

        enableEdgeToEdge()
        setContent {
            DisposableEffect(Unit) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "pref_systemTheme") {
                        themePref = sharedPrefs.getString("pref_systemTheme", "Light") ?: "Light"
                    }//if
                }//val
                sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }//DisposableEffect

            Csc481birdappTheme(
                themePref = themePref
            ) {
                //create controller for navigating screens
                val navController = rememberNavController()

                fun onBackReset(){
                    //reset everything in the viewModel
                    viewModel.isProcessing = false
                    viewModel.bitmap = null
                    viewModel.detections = emptyList()
                    viewModel.geoLat = null
                    viewModel.geoLon = null
                    viewModel.takenWithCamera = false
                    viewModel.bmpUri = null

                    //move back to home screen
                    navController.navigate("home")
                }//fun

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onCameraClick = {
                                    viewModel.takenWithCamera = true
                                    navController.navigate("bycamera")
                                },
                                onGalleryClick = {
                                    navController.navigate("bygallery")
                                },
                                onFileClick = {
                                    navController.navigate("byfile")
                                },
                                onFAQClick = {
                                    navController.navigate("faq")
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
                                }
                            )///HomeScreen
                        }//composable
                        composable("bycamera") {
                            DetectByCameraScreen(
                                viewModel,
                                onDetectionsComplete = {
                                    navController.navigate("results")
                                },
                                onBack = {
                                    onBackReset()
                                },
                                prefs = sharedPrefs
                            )//DetectByCameraScreen
                        }//composable
                        composable("bygallery") {
                            DetectByGalleryScreen(
                                viewModel,
                                sharedPrefs,
                                onDetectionsComplete = {
                                    navController.navigate("results")
                                },
                                onBack = {
                                    onBackReset()
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
                                    onBackReset()
                                }//onBack
                            )//ChooseFileScreen
                        }//composable
                        composable("results") {
                            ResultsScreen(
                                viewModel,
                                onBack = {
                                    onBackReset()
                                },
                                prefs = sharedPrefs
                            )//ResultsScreen
                        }//composable
                        composable("faq") {
                            FAQScreen(
                                onBack = {
                                    //no need for ViewModel management
                                    navController.navigate("home")
                                }//onBack
                            )//FAQScreen
                        }//composable
                        composable("settings") {
                            SettingsScreen(
                                prefs = sharedPrefs,
                                onBack = {
                                    //no need for ViewModel management
                                    navController.navigate("home")
                                }//onBack
                            )//SettingsScreen
                        }//composable
                    }//NavHost
                }//Surface
            }//Theme
        }//setContent
    }//override fun
}//class