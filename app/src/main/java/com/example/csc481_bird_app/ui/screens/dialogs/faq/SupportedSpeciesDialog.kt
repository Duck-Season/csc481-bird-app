package com.example.csc481_bird_app.ui.screens.dialogs.faq

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.csc481_bird_app.R
import org.json.JSONObject

@Composable
fun SupportedSpeciesDialog(
    context: Context,
    hideDialog: () -> Unit,
    onSpeciesClick: (String) -> Unit
){

    //get the JSON file with the info
    val jsonString = context.assets.open("csc481_specieslinks.json")
        .bufferedReader()
        .use { it.readText() }
    val jsonObj = JSONObject(jsonString)
    val speciesList = jsonObj.keys().asSequence().toList().sorted()

    //the actual Composable
    AlertDialog(
        icon = {
            Icon(painter = painterResource(id = R.drawable.outline_checklist_24), contentDescription = "Supported Species List")
        },
        title = {
            Text("Supported Species")
        },
        text = {
            Column() {
                Text("Tap on a species' name to learn more about it!")

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(speciesList) { speciesName ->
                        ListItem(
                            headlineContent = { Text(speciesName) },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSpeciesClick(speciesName)
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