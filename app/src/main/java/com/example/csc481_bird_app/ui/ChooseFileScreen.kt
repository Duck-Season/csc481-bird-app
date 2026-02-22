package com.example.csc481_bird_app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import com.example.csc481_bird_app.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.csc481_bird_app.detector.DectectionsViewModel
import com.example.csc481_bird_app.filesaving.loadDetections
import com.example.csc481_bird_app.utils.getImageUriFromSave
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseFileScreen(
    viewModel: DectectionsViewModel,
    onDetectionsComplete: () -> Unit,
    onBack: () -> Unit
){
    //load in the list of files
    //filter ONLY by those beginning with "save_"
    val context = LocalContext.current
    val listSaves = remember {
        context.filesDir.listFiles()
            ?.filter { file ->
                //check for valid save name prefix
                if (file.name.startsWith("save_")) {
                    val uri = getImageUriFromSave(context, file.name)

                    //check if URI is null or if the image file it points to is missing
                    val imageExists = uri?.let {
                        try {
                            context.contentResolver.openInputStream(it)?.use { true } ?: false
                        } catch (e: Exception) { false }
                    } ?: false

                    //delete the save file if the image is gone
                    if (!imageExists) {
                        file.delete()
                        false
                    } else true
                } else false
            }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }//val remember

    //mutable values
    var selectedIndex by remember { mutableStateOf<Int>(-1)}

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    if(selectedIndex == -1){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ){
                            TextButton(
                                onClick = onBack,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.rounded_arrow_back_24),
                                    contentDescription = "Back Arrow"
                                )//Icon
                            }//TextButton

                            Text("Choose a Saved Scan")
                        }//Row
                    }else{
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ){
                            TextButton(
                                onClick = {
                                    selectedIndex = -1
                                }//onClick
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.rounded_close_24),
                                    contentDescription = "Cancel Selection"
                                )//Icon
                            }//TextButton

                            Button(
                                onClick = {
                                    loadDetections(context, listSaves[selectedIndex].name, viewModel)
                                    onDetectionsComplete()
                                }//onClick
                            ) {
                                Row() {
                                    Text("Load Scan")
                                    Icon(
                                        painter = painterResource(id = R.drawable.rounded_arrow_forward_24),
                                        contentDescription = "Load Selected File"
                                    )//Icon
                                }//Row
                            }//FilledTonalButton
                        }//Row
                    }//if-else
                }//title
            )//TopAppBar
        },
    ) { innerPadding ->
        //Lazy Column to hold everything in place
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(listSaves.isEmpty()){
                item{
                    Card() {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "No scans found...",
                                fontSize = 30.sp
                            )//Text
                            Text("Start by taking a picture or by opening your photo gallery!")
                        } //Column
                    }//Card
                }//item
            }else{
                //make a list of files
                itemsIndexed(listSaves){ index, save ->
                    val name = save.name

                    //convert epoch time in name to readable format
                    val previewName = convertEpochDateToReadable(
                        name.substring(name.indexOf("_") + 1, name.length)
                    )//val

                    val imgUri = getImageUriFromSave(context, name)

                    //file entry
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        onClick = {
                            selectedIndex = index
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedIndex == index) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }//if-else
                        )//colors
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ){
                            //a preview of the image is more intuitive than a date
                            AsyncImage(
                                model = getImageUriFromSave(context, name), // Get the URI/File instead of Bitmap
                                contentDescription = "Preview",
                                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                            )//AsyncImage

                            //use the readable time name
                            Text(previewName)
                        }//Row
                    }//Card
                }//itemsIndexed
            }//if-else
        }//LazyColumn
    }//Scaffold
}//fun

fun convertEpochDateToReadable(strDate: String): String{
    val epochMillis = strDate.toLongOrNull() ?: return "Unknown Date"
    val date = java.util.Date(epochMillis)
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
}//fun