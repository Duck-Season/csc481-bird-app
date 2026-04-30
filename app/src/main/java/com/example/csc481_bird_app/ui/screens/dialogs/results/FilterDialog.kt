package com.example.csc481_bird_app.ui.screens.dialogs.results

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.csc481_bird_app.R
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog (
    context: Context,
    hideDialog: () -> Unit,
    filterSet: Set<String>,
    onFilterSetChange: (Set<String>) -> Unit,
    isFavoritingMode: Boolean = false
) {
    //get the JSON file with the info
    //get a list of bird species names
    val jsonString = context.assets.open("csc481_specieslinks.json")
        .bufferedReader()
        .use { it.readText() }
    val jsonObj = JSONObject(jsonString)
    val speciesList = jsonObj.keys().asSequence().toList().sorted()

    //mutables
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    val searchedItems = speciesList.filter{ it.contains(searchQuery, ignoreCase = true) }

    //the actual Composable
    AlertDialog(
        modifier = Modifier.height(600.dp),
        icon = {
            if(!isFavoritingMode) Icon(painter = painterResource(id = R.drawable.outline_filter_alt_24), contentDescription = null)
        },
        title = {
            Text(
                text = if(isFavoritingMode) "Favorite Species" else "Filter Species"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ){
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it  },
                    onSearch = { active = false },
                    active = active,
                    onActiveChange = {active = it},

                    modifier = Modifier
                        .padding(start = 12.dp, top = 2.dp, end = 12.dp, bottom = 12.dp)
                        .fillMaxWidth(),

                    placeholder = { Text("Search") },

                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_search_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )//Icon
                    },
                    trailingIcon = {
                        if (active)
                            Icon(
                                painter = painterResource(id = R.drawable.rounded_close_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )//Icon
                    },
                    colors = SearchBarDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                    tonalElevation = 0.dp,
                ) {
                    LazyColumn() {
                        items(speciesList.filter { it.contains(searchQuery, true) }) { speciesName ->
                            val isInSet = filterSet.contains(speciesName)

                            ListItem(
                                headlineContent = { Text(speciesName) },
                                modifier = Modifier.clickable {
                                    if (isInSet) {
                                        onFilterSetChange(filterSet - speciesName)
                                    } else onFilterSetChange(filterSet + speciesName)
                                    searchQuery = ""
                                    active = false
                                }//modifier
                            )//ListItem
                        }//items
                    }//LazyColumn
                }//SearchBar

                LazyHorizontalStaggeredGrid(
                    rows = StaggeredGridCells.Adaptive(20.dp),
                    horizontalItemSpacing = 2.dp,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if(filterSet.isNotEmpty()) 60.dp else 0.dp)
                ) {
                    items(filterSet.size){
                        val speciesName = filterSet.elementAt(it)

                        InputChip(
                            modifier = Modifier.height(12.dp),
                            onClick = {
                                onFilterSetChange(filterSet - speciesName)
                            },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                labelColor = MaterialTheme.colorScheme.surface
                            ),
                            label = { Text(speciesName) },
                            selected = true,
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.rounded_close_24),
                                    contentDescription = "Localized description",
                                )//Icon
                            }//trailingIcon
                        )//InputChip
                    }//items
                }//LazyHorizontalStaggeredGrid

                Spacer(modifier = Modifier.size(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(speciesList) { speciesName ->
                        val isInSet = filterSet.contains(speciesName)

                        ListItem(
                            headlineContent = {
                                Row(){
                                    if(isInSet) Icon(painter = painterResource(id = R.drawable.outline_check_24), contentDescription = null)
                                    Text(speciesName)
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            ),
                            modifier = Modifier.clickable {
                                if (isInSet) {
                                    onFilterSetChange(filterSet - speciesName)
                                } else onFilterSetChange(filterSet + speciesName)
                            }//modifier
                        )//ListItem
                    }//items
                }//LazyColumn
            }//Column
        },
        onDismissRequest = {
            hideDialog()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    //hide dialog
                    hideDialog()
                }//onClick
            ) {
                Text("Close")
            }//TextButton
        }//confirmButton
    )//AlertDialog
}//fun