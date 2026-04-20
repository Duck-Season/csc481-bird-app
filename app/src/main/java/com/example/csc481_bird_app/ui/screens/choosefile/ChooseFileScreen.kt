package com.example.csc481_bird_app.ui.screens.choosefile

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import com.example.csc481_bird_app.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csc481_bird_app.detector.DectectionsViewModel
import com.example.csc481_bird_app.filesaving.loadDetections
import com.example.csc481_bird_app.ui.screens.dialogs.choosefile.CreateFolderDialog
import com.example.csc481_bird_app.ui.screens.dialogs.choosefile.DeleteDialog
import com.example.csc481_bird_app.ui.screens.dialogs.choosefile.MoveFileDialog
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
    val context = LocalContext.current

    //mutable values
    var selectedIndex by remember { mutableStateOf(-1)}
    var selectedIsFile by remember { mutableStateOf(false)}
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var tempIsCamera by remember { mutableStateOf(false) }
    var currentDir by remember { mutableStateOf(context.filesDir) }

    //dialog mutable flags
    var showDeleteDialog by remember { mutableStateOf(false)}
    var showCreateFolderDialog by remember { mutableStateOf(false)}
    var showMoveFileDialog by remember { mutableStateOf(false)}

    //load in the list of folders
    val listFolders = remember {
        mutableStateListOf<File>().also { list ->
            context.filesDir.listFiles()
                ?.filter { it.isDirectory && it.name.startsWith("birdScans_") }
                ?.let { list.addAll(it) }
        }//.also
    }//val remember mutableStateList

    //load in the list of files
    val listSaves = remember { mutableStateListOf<File>() }
    LaunchedEffect(currentDir) {
        //clear existing list and counter
        listSaves.clear()

        val files = currentDir.listFiles()
            ?.filter { file ->
                //first, avoid accidentally deleting folder
                //check for valid save name prefix
                if (!file.isDirectory && file.name.startsWith("save_")) {
                    //check if URI is null or if the image file it points to is missing
                    //if 'uri?.let{}' doesn't run then the URI is assumed null
                    val uri = getImageUriFromSave(context, file.name, currentDir)
                    val imageExists = uri?.let {
                        try {
                            context.contentResolver.openInputStream(it)?.use { true } ?: false
                        } catch (e: Exception) { false }
                    } ?: false

                    //delete the save file if the image is gone
                    if (!imageExists) { file.delete(); false }
                    else true
                } else false
            }//.filter
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

        listSaves.addAll(files)
    }//val remember mutableStateList

    Scaffold(
        //------------ Top Bar ------------
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
                                if (currentDir == context.filesDir){
                                    onBack()
                                }else{
                                    currentDir = context.filesDir
                                }//if-else
                            }//onClick
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.rounded_arrow_back_24),
                                contentDescription = "Back Arrow"
                            )//Icon
                        }//TextButton

                        if(currentDir == context.filesDir){
                            Text("Choose a Saved Scan")

                            Spacer(
                                modifier = Modifier.weight(1f)
                            )//Spacer

                            //allow folder creation ONLY at the top level
                            TextButton(
                                onClick = {
                                    //display the dialog for deleting files
                                    showCreateFolderDialog = true
                                }//onClick
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_create_new_folder_24),
                                    contentDescription = "Create New Folder"
                                )//Icon
                            }//Button
                        }else{
                            //indicate we're in a folder
                            Text(currentDir.name.substringAfter("_"))
                        }//if-else
                    }//Row
                }//title
            )//TopAppBar
        },
    ) { innerPadding ->
        //------------ Files List ------------
        //Lazy Column to hold everything in place
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //the empty check depends on what directory we're in
            val isEmpty = if(currentDir == context.filesDir){
                listSaves.isEmpty() && listFolders.isEmpty()
            } else {
                listSaves.isEmpty()
            }//if-else

            if(isEmpty){
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
                //for each of these:
                // moreOptions has two options
                // 1. move to folder (or up out of current one)
                // 2. delete (only delete a folder when it's empty!)
                if(currentDir == context.filesDir){
                    //make a list of folders
                    itemsIndexed(listFolders){ index, folder ->
                        FileCard(
                            folder = folder,
                            fileName = folder.name.substringAfter("_"),
                            onSelection = {
                                currentDir = folder
                            },
                            onDelete = {
                                //set temporary selection value
                                selectedIndex = index
                                selectedIsFile = false
                                showDeleteDialog = true
                            },
                            //only allow folder deletion if folder is empty
                            isDeleteEnabled = folder.listFiles().size == 0
                        )//FileCard
                    }//itemsIndexed
                }//if

                //make a list of files
                itemsIndexed(listSaves){ index, save ->
                    val name = save.name
                    val nameSplits = name.split("_")

                    //boolean for indicator
                    //even if using a save with the old filename, will default to "false" (gallery mode)
                    val isCameraSave = nameSplits[1] == "camera"

                    //get Uri for image
                    val imgUri = getImageUriFromSave(context, name, currentDir)

                    //convert epoch time in name to readable format
                    val previewName = convertEpochDateToReadable(nameSplits[2])

                    FileCard(
                        fileName = previewName,
                        onSelection = {
                            //set temporary selection value
                            selectedIndex = index
                            tempIsCamera = isCameraSave
                            tempUri = imgUri

                            loadDetections(context, listSaves[selectedIndex].name, viewModel, tempUri, tempIsCamera, currentDir)
                            onDetectionsComplete()
                        },
                        imgUri = imgUri,
                        isCameraSave = isCameraSave,
                        onDelete = {
                            //set temporary selection value
                            selectedIndex = index
                            selectedIsFile = true
                            showDeleteDialog = true
                        },
                        onMove = {
                            selectedIndex = index
                            selectedIsFile = false
                            showMoveFileDialog = true
                        }
                    )//FileCard
                }//itemsIndexed
            }//if-else
        }//LazyColumn

        //popup to confirm deleting a file
        if(showDeleteDialog){
            DeleteDialog(
                hideDialog = {
                    showDeleteDialog = false
                },
                onChooseDelete = {
                    if (selectedIsFile) {
                        //delete the currently chosen file and remove it from display list
                        listSaves[selectedIndex].delete()
                        listSaves.removeAt(selectedIndex)
                    } else {
                        //delete the currently chosen folder and remove it from display list
                        listFolders[selectedIndex].delete()
                        listFolders.removeAt(selectedIndex)
                    }//if-else

                    //reset the selection
                    selectedIndex = -1
                }//onChooseDelete
            )//DeleteDialog
        }//if

        //popup to create a new folder
        if(showCreateFolderDialog){
            CreateFolderDialog(
                hideDialog = {
                    showCreateFolderDialog = false
                },
                onChooseCreate = { folderName: String ->
                    run {
                        //create new file with name
                        val createdFolder = File(context.filesDir, "birdScans_${folderName}")

                        //create a folder; if it works, add the folder to the list
                        if (createdFolder.mkdir()) listFolders.add(createdFolder)
                    }//fun
                }//onChooseCreate
            )//Dialog
        }//if

        //popup to move a file
        if(showMoveFileDialog){
            MoveFileDialog(
                context = context,
                hideDialog = {
                    showMoveFileDialog = false
                },
                onChooseMove = { destFolder ->
                    val fileToMove = listSaves[selectedIndex]
                    val destination = File(destFolder, fileToMove.name)

                    if (fileToMove.renameTo(destination)) {
                        listSaves.removeAt(selectedIndex)
                        selectedIndex = -1
                    }//if
                },
                listFolders = listOf(context.filesDir) + listFolders,
                currentFolder = currentDir
            )//Dialog
        }//if
    }//Scaffold
}//fun

fun convertEpochDateToReadable(strDate: String): String{
    val epochMillis = strDate.toLongOrNull() ?: return "Unknown Date"
    val date = Date(epochMillis)
    val formatter = SimpleDateFormat("MMMM dd, yyyy\n HH:mm:ss aaa", Locale.getDefault())
    return formatter.format(date)
}//fun