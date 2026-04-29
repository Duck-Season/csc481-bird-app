package com.example.csc481_bird_app.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.example.csc481_bird_app.R
import com.example.csc481_bird_app.ui.screens.dialogs.faq.SupportedSpeciesDialog
import com.example.csc481_bird_app.ui.screens.dialogs.results.SpeciesLinksDialog
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(
    onBack: () -> Unit,
){
    //get the JSON file with the info
    val jsonString = LocalContext.current.assets.open("csc481_faq.json")
        .bufferedReader()
        .use { it.readText() }
    val jsonArr = JSONArray(jsonString)
    val jsonList = (0 until jsonArr.length()).map { i ->
        jsonArr.getJSONObject(i)
    }//val

    //mutable vars
    var showSupportedSpeciesDialog by remember { mutableStateOf(false) }
    var showSpeciesLinkDialog by remember { mutableStateOf(false) }
    var selectedSupportedSpecies by remember { mutableStateOf("") }

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

                        Text("FAQ")
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
            items(jsonList){ section ->
                //load the title of the section
                val title = section.getString("title")
                val contents = section.getJSONArray("contents")

                //make a title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )//Text

                HorizontalDivider(
                    modifier = Modifier.width(200.dp)
                )//HorizontalDivider
                Spacer(
                    modifier = Modifier.padding(8.dp)
                )//Spacer

                //go through the contents
                for(i in 0 until contents.length()){
                    val content = contents.getJSONObject(i)

                    //get the subheader
                    val subheader = content.optString("subheader", "")
                    if (subheader.isNotEmpty()) {
                        Text(
                            text = subheader,
                            style = MaterialTheme.typography.headlineSmall
                        )//Text

                        Spacer(
                            modifier = Modifier.padding(8.dp)
                        )//Spacer
                    }//if

                    //check for "supported species" section
                    val isSpeciesListSection = content.optString("isSpeciesListSection", "") == "true"
                    if(isSpeciesListSection){
                        ElevatedButton(
                            colors = ButtonDefaults.buttonColors(),
                            onClick = {
                                showSupportedSpeciesDialog = true
                            }//onClick
                        ) {
                            Row() {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_checklist_24),
                                    contentDescription = "Show Supported Species Icon"
                                )//Icon

                                Spacer(Modifier.width(8.dp))

                                Text("Show Supported Species")
                            }//Row
                        }//ElevatedButton
                    }//if

                    //get the paragraphs
                    val subitems = content.optJSONArray("subitems")
                    subitems?.let {
                        for (j in 0 until it.length()) {
                            //check for "two-links" format
                            val isTwoLinker = it.getJSONObject(j).optString("hasTwoLinks", "").isNotEmpty()

                            //check for "two links" format in JSON
                            //(i.e. "this app uses [thing] from [developer]")
                            if(isTwoLinker){
                                val text1 = it.getJSONObject(j).optString("text1", "")
                                val link1 = it.getJSONObject(j).optString("link1", "")
                                val url1 = it.getJSONObject(j).optString("url1", "")
                                val text2 = it.getJSONObject(j).optString("text2", "")
                                val link2 = it.getJSONObject(j).optString("link2", "")
                                val url2 = it.getJSONObject(j).optString("url2", "")

                                Text(
                                    text = buildAnnotatedString {
                                        append("$text1 ")
                                        withLink(
                                            LinkAnnotation.Url(
                                                url1,
                                                TextLinkStyles(style = SpanStyle(color = Color.Blue))
                                            )//.Url
                                        ) {
                                            append(link1)
                                        }//withLink
                                        append(" $text2 ")
                                        withLink(
                                            LinkAnnotation.Url(
                                                url2,
                                                TextLinkStyles(style = SpanStyle(color = Color.Blue))
                                            )//.Url
                                        ) {
                                            append(link2)
                                        }//withLink
                                        append(".\n")
                                    },
                                    modifier = Modifier.padding(start = 16.dp)
                                )//Text
                            }else{
                                //is just a regular paragraph
                                val text = it.getJSONObject(j).optString("text", "")
                                Text(text = text, modifier = Modifier.padding(start = 16.dp))
                            }//if-else
                        }//for

                        Spacer(
                            modifier = Modifier.padding(8.dp)
                        )//Spacer
                    }//.let

                    Spacer(
                        modifier = Modifier.padding(16.dp)
                    )//Spacer
                }//for
            }//items
        }//LazyColumn

        if(showSpeciesLinkDialog){
            SpeciesLinksDialog(
                hideDialog = {
                    //reset vars
                    showSpeciesLinkDialog = false
                    selectedSupportedSpecies = ""
                },
                speciesName = selectedSupportedSpecies
            )//SpeciesLinksDialog
        }//if

        if(showSupportedSpeciesDialog){
            SupportedSpeciesDialog(
                context = LocalContext.current,
                hideDialog = {
                    showSupportedSpeciesDialog = false
                },
                onSpeciesClick = { birdName: String ->
                    showSpeciesLinkDialog = true
                    selectedSupportedSpecies = birdName
                }//onSpeciesClick
            )//SupportedSpeciesDialog
        }//if
    }//Scaffold
}//composable fun