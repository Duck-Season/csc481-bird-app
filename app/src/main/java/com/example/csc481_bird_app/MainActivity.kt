package com.example.csc481_bird_app

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = DectectionsViewModel(application)

        //needed for managing preferences
        val sharedPrefs = getPreferences(MODE_PRIVATE)
        var themePref by mutableStateOf(sharedPrefs.getString("pref_systemTheme", "Light") ?: "Light")
        var altLayoutPref by mutableStateOf(sharedPrefs.getBoolean("pref_useAlternateLayout", false))
        var chosenScreen by mutableStateOf("")
        var isNavbarEnabled by mutableStateOf(true)

        enableEdgeToEdge()
        setContent {
            DisposableEffect(Unit) {
                val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "pref_systemTheme") {
                        themePref = sharedPrefs.getString("pref_systemTheme", "Light") ?: "Light"
                    }//if

                    if (key == "pref_useAlternateLayout") {
                        altLayoutPref = sharedPrefs.getBoolean("pref_useAlternateLayout", false)
                    }//if
                }//val
                sharedPrefs.registerOnSharedPreferenceChangeListener(prefsListener)
                onDispose { sharedPrefs.unregisterOnSharedPreferenceChangeListener(prefsListener) }
            }//DisposableEffect

            Csc481birdappTheme(
                themePref = themePref
            ) {
                //create controller for navigating screens
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute by remember { derivedStateOf { currentBackStackEntry?.destination?.route ?: "home" } }
                var isInResults = currentRoute == "results"

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

                    //re-enable the navbar buttons
                    isNavbarEnabled = true
                }//fun

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            if(altLayoutPref && currentRoute == "home"){
                                TopAppBar(
                                    colors = topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    title = {
                                        Text("Gander: Bird Detection App")
                                    }//title
                                )//TopAppBar
                            }
                        },
                        bottomBar = {
                            if(altLayoutPref && !isInResults){
                                NavigationBar {
                                    NavigationBarItem(
                                        selected = (chosenScreen == "byfile"),
                                        onClick = {
                                            chosenScreen = "byfile"
                                            navController.navigate("byfile")
                                        },
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.rounded_files_24),
                                                contentDescription = "Collections"
                                            )
                                        },
                                        label = { Text("Collections") },
                                        enabled = isNavbarEnabled
                                    )
                                    NavigationBarItem(
                                        selected = (chosenScreen == "bycamera"),
                                        onClick = {
                                            viewModel.takenWithCamera = true
                                            chosenScreen = "bycamera"
                                            navController.navigate("bycamera")
                                        },
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.rounded_add_camera_24),
                                                contentDescription = "Camera"
                                            )
                                        },
                                        label = { Text("Camera") },
                                        enabled = isNavbarEnabled
                                    )
                                    NavigationBarItem(
                                        selected = (chosenScreen == "bygallery"),
                                        onClick = {
                                            chosenScreen = "bygallery"
                                            navController.navigate("bygallery")
                                        },
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.rounded_add_photo_alternate_24),
                                                contentDescription = "Gallery"
                                            )
                                        },
                                        label = { Text("Gallery") },
                                        enabled = isNavbarEnabled
                                    )
                                    NavigationBarItem(
                                        selected = chosenScreen == "faq",
                                        onClick = {
                                            chosenScreen = "faq"
                                            navController.navigate("faq")
                                        },
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.outline_help_24),
                                                contentDescription = "Help"
                                            )
                                        },
                                        label = { Text("Help") },
                                        enabled = isNavbarEnabled
                                    )
                                    NavigationBarItem(
                                        selected = (chosenScreen == "settings"),
                                        onClick = {
                                            chosenScreen = "settings"
                                            navController.navigate("settings")
                                        },
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_settings_24),
                                                contentDescription = "Settings"
                                            )
                                        },
                                        label = { Text("Settings") },
                                        enabled = isNavbarEnabled
                                    )
                                }//NavigationBar
                            }//if
                        }//bottomBar
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            composable("home") {
                                if(altLayoutPref){
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Welcome to Gander!\n\nUse the navigation bar below to get started.",
                                            style = MaterialTheme.typography.titleLarge,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(24.dp)
                                        )
                                    }//Column
                                }else{
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
                                        },
                                        outerPadding = innerPadding
                                    )///HomeScreen
                                }//if-else
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
                                    },
                                    disableNavbar = {
                                        isNavbarEnabled = false
                                    }
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
                    }//Scaffold
                }//Surface
            }//Theme
        }//setContent
    }//override fun
}//class