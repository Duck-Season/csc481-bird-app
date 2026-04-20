package com.example.csc481_bird_app.ui.screens

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.csc481_bird_app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: SharedPreferences,
    onBack: () -> Unit
){
    //checkbox remember mutables
    //settings candidates:
    // - disable automatic detection saving
    // - enable dark mode
    // - manage favorited species (manage collections of favorites?)
    var cbAutoOpenGalleryPicker by remember { mutableStateOf(prefs.getBoolean("pref_autoOpenGalleryPicker", true)) }
    var cbAutosavingEnabled by remember { mutableStateOf(prefs.getBoolean("pref_autosavingEnabled", true)) }
    var cbAutosaveEmptyScans by remember { mutableStateOf(prefs.getBoolean("pref_autosaveEmptyScans", false)) }
    var cbDarkModeEnabled by remember { mutableStateOf(prefs.getBoolean("pref_darkModeEnabled", false)) }

    //the actual Composable
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ){
                        TextButton(
                            onClick = {
                                onBack()
                            }//onClick
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.rounded_arrow_back_24),
                                contentDescription = "Back Arrow"
                            )//Icon
                        }//TextButton

                        Text("Settings")
                    }//Row
                }//title
            )//TopAppBar
        }//topBar
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column() {
                    Text(
                        text = "Gallery Mode",
                        style = MaterialTheme.typography.headlineMedium
                    )//Text

                    HorizontalDivider(
                        modifier = Modifier.width(200.dp)
                    )//HorizontalDivider
                    Spacer(
                        modifier = Modifier.padding(4.dp)
                    )//Spacer

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text("Open Gallery Picker Automatically")
                        Checkbox(
                            checked = cbAutoOpenGalleryPicker,
                            onCheckedChange = {
                                //set checkbox
                                cbAutoOpenGalleryPicker = it

                                //create editor for preferences
                                val editor = prefs.edit()
                                editor.putBoolean("pref_autoOpenGalleryPicker", it)
                                editor.apply()
                            }//onCheckedChange
                        )//Checkbox
                    }//Row

                    Spacer(
                        modifier = Modifier.padding(4.dp)
                    )//Spacer

                    Text(
                        text = "File Saving",
                        style = MaterialTheme.typography.headlineMedium
                    )//Text

                    HorizontalDivider(
                        modifier = Modifier.width(200.dp)
                    )//HorizontalDivider
                    Spacer(
                        modifier = Modifier.padding(4.dp)
                    )//Spacer

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text("Save scans automatically")
                        Checkbox(
                            checked = cbAutosavingEnabled,
                            onCheckedChange = {
                                //set checkbox
                                cbAutosavingEnabled = it

                                //create editor for preferences
                                val editor = prefs.edit()
                                editor.putBoolean("pref_autosavingEnabled", it)
                                editor.apply()
                            }//onCheckedChange
                        )//Checkbox
                    }//Row

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ){
                        Text(
                            text = "Save empty scans automatically",
                            modifier = Modifier.alpha(if (cbAutosavingEnabled) 1f else 0.5f)
                        )
                        Checkbox(
                            checked = cbAutosaveEmptyScans,
                            enabled = cbAutosavingEnabled,
                            onCheckedChange = {
                                //set checkbox
                                cbAutosaveEmptyScans = it

                                //create editor for preferences
                                val editor = prefs.edit()
                                editor.putBoolean("pref_autosaveEmptyScans", it)
                                editor.apply()
                            }//onCheckedChange
                        )//Checkbox
                    }//Row
                }//Column
            }//item
        }//LazyColumn
    }//Scaffold
}//composable fun