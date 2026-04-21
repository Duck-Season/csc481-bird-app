package com.example.csc481_bird_app.ui.screens.dialogs.results

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.csc481_bird_app.R
import org.json.JSONObject

@Composable
fun SpeciesLinksDialog (
    hideDialog: () -> Unit,
    speciesName: String
) {
    val context = LocalContext.current

    //get the JSON file with the info
    val jsonString = context.assets.open("csc481_specieslinks.json")
        .bufferedReader()
        .use { it.readText() }
    val jsonObj = JSONObject(jsonString)
    val jsonSpecies = jsonObj.optJSONObject(speciesName)
    val speciesCheck = jsonObj.optString(speciesName, "")

    //the actual Composable
    AlertDialog(
        icon = {
            Icon(painter = painterResource(id = R.drawable.outline_open_in_browser_24), contentDescription = "Learn More")
        },
        title = {
            Text("Learn More")
        },
        text = {
            Column() {
                if(jsonSpecies != null){
                    Text("Choose one of the links below to learn more about the $speciesName.")

                    //iNat link
                    ElevatedButton(
                        colors = ButtonDefaults.buttonColors(),
                        onClick = {
                            //check for the link to iNaturalist and open it if it exists
                            val link_iNat = jsonSpecies.optString("iNaturalist", "")
                            if (link_iNat.isNotEmpty()) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link_iNat)))
                            }//if
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logo_inaturalist),
                            contentDescription = "iNaturalist Logo",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )//AsyncImage
                    }//TextButton

                    Spacer(
                        modifier = Modifier.padding(4.dp)
                    )//Spacer

                    //AllAboutBirds link
                    ElevatedButton(
                        colors = ButtonDefaults.buttonColors(),
                        onClick = {
                            //check for the link to All About Birds and open it if it exists
                            val link_AAB = jsonSpecies.optString("AllAboutBirds", "")
                            if (link_AAB.isNotEmpty()) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link_AAB)))
                            }//if
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.logo_allaboutbirds),
                            contentDescription = "AllAboutBirds Logo",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )//AsyncImage
                    }//TextButton
                }else{
                    Text("No links found!")
                }//if-else
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
                Text("Cancel")
            }//TextButton
        }
    )//AlertDialog
}//fun