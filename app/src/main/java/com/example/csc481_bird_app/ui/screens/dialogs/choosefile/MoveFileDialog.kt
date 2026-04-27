package com.example.csc481_bird_app.ui.screens.dialogs.choosefile

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.csc481_bird_app.R
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveFileDialog (
    context: Context,
    hideDialog: () -> Unit,
    onChooseMove: (folder: File) -> Unit,
    listFolders: List<File>,
    currentFolder: File
) {
    //mutable vars
    var expanded by remember { mutableStateOf(false) }

    //there's always going to be at least one folder (context.filesDir)
    var chosenFolder by remember { mutableStateOf(listFolders[0]) }

    AlertDialog(
        icon = {
            Icon(painter = painterResource(id = R.drawable.outline_drive_file_move_24), contentDescription = "Move Selected File")
        },
        title = {
            Text("Move File")
        },
        text = {
            Column() {
                Text("Where would you like to move this file?")

                val folderName = if(chosenFolder == context.filesDir) {
                    "No Folder"
                }else {
                    chosenFolder.name.substringAfter("_")
                }//val if-else

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = folderName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )//TextField

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listFolders.forEach { folder ->
                            val folderName = if(folder == context.filesDir) {
                                "No Folder"
                            }else {
                                folder.name.substringAfter("_")
                            }//val if-else

                            DropdownMenuItem(
                                text = {
                                    Row() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_folder_24),
                                            contentDescription = null
                                        )//Icon
                                        Text(folderName)
                                    }//Row
                                },
                                enabled = folder != currentFolder,
                                onClick = {
                                    chosenFolder = folder
                                    expanded = false
                                }//onClick
                            )//DropdownMenuItem
                        }//.forEach
                    }//ExposedDropdownMenu
                }//ExposedDropdownMenuBox
            }//Column
        },
        onDismissRequest = {hideDialog()},
        confirmButton = {
            TextButton(
                onClick = {
                    //move the file
                    onChooseMove(chosenFolder)

                    //close the dialog
                    hideDialog()
                }//onClick
            ) {
                Text("Move")
            }//TextButton
        },
        dismissButton = {
            TextButton(
                onClick = {
                    hideDialog()
                }//onClick
            ) {
                Text("Cancel")
            }//TextButton
        }//dismissButton
    )//AlertDialog
}//fun