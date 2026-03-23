package com.example.csc481_bird_app.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import com.example.csc481_bird_app.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateListOf
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
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
        mutableStateListOf<File>()
    }//val remember mutableStateList
    listSaves.addAll(
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
            }//.filter
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    )

    //mutable values
    var selectedIndex by remember { mutableStateOf(-1)}
    var showDeleteDialog by remember { mutableStateOf(false)}
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var tempIsCamera by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    if(selectedIndex == -1){
                        //nothing currently selected
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
                        //a file is picked by the user
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

                            Spacer(
                                modifier = Modifier.weight(1f)
                            )

                            TextButton(
                                onClick = {
                                    //display the dialog for deleting files
                                    showDeleteDialog = true
                                }//onClick
                            ) {
                                Row() {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_delete_24),
                                        contentDescription = "Delete Selected File"
                                    )//Icon
                                }//Row
                            }//Button

                            TextButton(
                                onClick = {
                                    loadDetections(context, listSaves[selectedIndex].name, viewModel, tempUri, tempIsCamera)
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
                            }//Button
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
                    val nameSplits = name.split("_")

                    //boolean for indicator
                    //even if using a save with the old filename, will default to "false" (gallery mode)
                    val isCameraSave = nameSplits[1] == "camera"

                    //get Uri for image
                    val imgUri = getImageUriFromSave(context, name)

                    //convert epoch time in name to readable format
                    val previewName = convertEpochDateToReadable(
                        nameSplits[2]
                    )//val

                    //file entry
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        onClick = {
                            //set temporary selection values
                            if(selectedIndex == index){
                                selectedIndex = -1
                            }else{
                                //set temporary selection values
                                selectedIndex = index
                                tempIsCamera = isCameraSave
                                tempUri = imgUri
                            }//if-else
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedIndex == index) {
                                MaterialTheme.colorScheme.primaryFixedDim
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }//if-else
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ){
                            //a preview of the image is more intuitive than a date
                            AsyncImage(
                                model = imgUri, // Get the URI/File instead of Bitmap
                                contentDescription = "Preview",
                                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                            )//AsyncImage

                            //use the readable time name
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ){
                                Text(previewName)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    painter = painterResource(id = if(isCameraSave) R.drawable.rounded_add_camera_24 else R.drawable.rounded_add_photo_alternate_24),
                                    contentDescription = "Save taken with phone " + if(isCameraSave) "camera" else "gallery"
                                )//Icon
                            }//Row
                        }//Row
                    }//Card
                }//itemsIndexed
            }//if-else
        }//LazyColumn

        //popup to confirm deleting a file
        if(showDeleteDialog){
            AlertDialog(
                icon = {
                    Icon(painter = painterResource(id = R.drawable.outline_delete_24), contentDescription = "Delete Selected File")
                },
                title = {
                    Text("Delete File?")
                },
                text = {
                    Text("Are you sure you want to delete this file from your saved scans?")
                },
                onDismissRequest = {
                    showDeleteDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            //delete the currently chosen file and remove it from display list
                            listSaves[selectedIndex].delete()
                            listSaves.removeAt(selectedIndex)

                            //reset the selection
                            selectedIndex = -1

                            //hide dialog
                            showDeleteDialog = false
                        }//onClick
                    ) {
                        Text("Yes")
                    }//TextButton
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                        }//onClick
                    ) {
                        Text("No")
                    }//TextButton
                }//dismissButton
            )//AlertDialog
        }//if
    }//Scaffold
}//fun

fun convertEpochDateToReadable(strDate: String): String{
    val epochMillis = strDate.toLongOrNull() ?: return "Unknown Date"
    val date = Date(epochMillis)
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
}//fun