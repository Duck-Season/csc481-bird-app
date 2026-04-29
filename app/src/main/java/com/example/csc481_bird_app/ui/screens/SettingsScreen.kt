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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.csc481_bird_app.R
import com.example.csc481_bird_app.ui.screens.dialogs.results.FilterDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: SharedPreferences,
    onBack: () -> Unit
){
    val context = LocalContext.current

    //checkbox remember mutables
    //settings candidates:
    // - manage favorited species (manage collections of favorites?)
    var cbAutoOpenGalleryPicker by remember { mutableStateOf(prefs.getBoolean("pref_autoOpenGalleryPicker", true)) }
    var cbAutosavingEnabled by remember { mutableStateOf(prefs.getBoolean("pref_autosavingEnabled", true)) }
    var cbAutosaveEmptyScans by remember { mutableStateOf(prefs.getBoolean("pref_autosaveEmptyScans", false)) }
    var cbUseAlternateLayout by remember { mutableStateOf(prefs.getBoolean("pref_useAlternateLayout", false)) }

    //dropdown remember mutables
    var ddSystemThemeExpanded by remember { mutableStateOf(false)}
    var ddSystemThemeSelected by remember { mutableStateOf(prefs.getString("pref_systemTheme", "Light"))}

    //favoriting set
    var showFavoritingDialog by remember { mutableStateOf(false) }
    var favoritesSet by remember { mutableStateOf(prefs.getStringSet("prefs_favoriteSpecies", setOf<String>())) }

    //launchedEffect for setting favorite species preferences
    LaunchedEffect(favoritesSet) {
        prefs.edit().putStringSet("prefs_favoriteSpecies", favoritesSet).apply()
    }//LaunchedEffect

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
                        IconButton(
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
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text("Open Gallery Picker Automatically")

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )//Spacer

                        Switch(
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
                        modifier = Modifier.padding(16.dp)
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
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text("Save scans automatically")

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )//Spacer

                        Switch(
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
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = "Save empty scans automatically",
                            modifier = Modifier.alpha(if (cbAutosavingEnabled) 1f else 0.5f)
                        )

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )//Spacer

                        Switch(
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

                    Spacer(
                        modifier = Modifier.padding(16.dp)
                    )//Spacer

                    Text(
                        text = "Results Screen",
                        style = MaterialTheme.typography.headlineMedium
                    )//Text

                    HorizontalDivider(
                        modifier = Modifier.width(200.dp)
                    )//HorizontalDivider
                    Spacer(
                        modifier = Modifier.padding(4.dp)
                    )//Spacer

                    ElevatedButton(
                        colors = ButtonDefaults.buttonColors(),
                        onClick = {
                            showFavoritingDialog = true
                        }//onClick
                    ) {
                        Row() {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_checklist_24),
                                contentDescription = "Change Favorite Species Button"
                            )//Icon

                            Spacer(Modifier.width(8.dp))

                            Text("Change Favorite Species")
                        }//Row
                    }//ElevatedButton

                    Spacer(
                        modifier = Modifier.padding(16.dp)
                    )//Spacer

                    Text(
                        text = "Themes and UI",
                        style = MaterialTheme.typography.headlineMedium
                    )//Text

                    HorizontalDivider(
                        modifier = Modifier.width(200.dp)
                    )//HorizontalDivider
                    Spacer(
                        modifier = Modifier.padding(4.dp)
                    )//Spacer

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text("App Theme")

                        Spacer(
                            modifier = Modifier.padding(48.dp)
                        )//Spacer

                        ExposedDropdownMenuBox(
                            expanded = ddSystemThemeExpanded,
                            onExpandedChange = { ddSystemThemeExpanded = !ddSystemThemeExpanded }
                        ) {
                            TextField(
                                value = ddSystemThemeSelected!!,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = ddSystemThemeExpanded)
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )//TextField

                            ExposedDropdownMenu(
                                expanded = ddSystemThemeExpanded,
                                onDismissRequest = { ddSystemThemeExpanded = false }
                            ) {

                                DropdownMenuItem(
                                    text = {
                                        Text("Light")
                                    },
                                    onClick = {
                                        val editor = prefs.edit()
                                        editor.putString("pref_systemTheme", "Light")
                                        editor.apply()

                                        ddSystemThemeSelected = "Light"
                                        ddSystemThemeExpanded = false
                                    }//onClick
                                )//DropdownMenuItem

                                DropdownMenuItem(
                                    text = {
                                        Text("Dark")
                                    },
                                    onClick = {
                                        val editor = prefs.edit()
                                        editor.putString("pref_systemTheme", "Dark")
                                        editor.apply()

                                        ddSystemThemeSelected = "Dark"
                                        ddSystemThemeExpanded = false
                                    }//onClick
                                )//DropdownMenuItem

                                DropdownMenuItem(
                                    text = {
                                        Text("System")
                                    },
                                    onClick = {
                                        val editor = prefs.edit()
                                        editor.putString("pref_systemTheme", "System")
                                        editor.apply()

                                        ddSystemThemeSelected = "System"
                                        ddSystemThemeExpanded = false
                                    }//onClick
                                )//DropdownMenuItem
                            }//ExposedDropdownMenu
                        }//ExposedDropdownMenuBox
                    }//Row

                    Spacer(
                        modifier = Modifier.padding(4.dp)
                    )//Spacer

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(text = "Use alternate layout")

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )//Spacer

                        Switch(
                            checked = cbUseAlternateLayout,
                            onCheckedChange = {
                                //set checkbox
                                cbUseAlternateLayout = it

                                //create editor for preferences
                                val editor = prefs.edit()
                                editor.putBoolean("pref_useAlternateLayout", it)
                                editor.apply()
                            }//onCheckedChange
                        )//Checkbox
                    }//Row
                }//Column
            }//item
        }//LazyColumn

        if(showFavoritingDialog){
            FilterDialog(
                context = context,
                hideDialog = {
                    showFavoritingDialog = false
                },
                filterSet = favoritesSet!!.toSet(),
                onFilterSetChange = { favoritesSet = it },
                isFavoritingMode = true
            )//FilterDialog
        }//if
    }//Scaffold
}//composable fun