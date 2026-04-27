package com.example.csc481_bird_app.ui.screens.dialogs.choosefile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.csc481_bird_app.R

@Composable
fun CreateFolderDialog (
    hideDialog: () -> Unit,
    onChooseCreate: (enteredName: String) -> Unit,
) {
    //general vals
    val nameRegex = Regex("[a-zA-Z0-9_]+")

    //mutable vars
    val nameFieldState = rememberTextFieldState(initialText = "New_Folder")
    val folderName = nameFieldState.text.toString()
    val isValidName = folderName.matches(nameRegex)

    AlertDialog(
        icon = {
            Icon(painter = painterResource(id = R.drawable.outline_delete_24), contentDescription = "Make New Folder")
        },
        title = {
            Text("Create Folder")
        },
        text = {
            Column() {
                Text("Please give the folder a name:")
                TextField(
                    state = nameFieldState,
                    label = { Text("Folder Name") },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    isError = !isValidName,
                    supportingText = {
                        if (!isValidName) Text("Invalid name for folder. Only letters, numbers, or underscores allowed.", color = Color.Red)
                    }//supportingText
                )//TextField
            }//Column
        },
        onDismissRequest = {hideDialog()},
        confirmButton = {
            TextButton(
                enabled = isValidName,
                onClick = {
                    //make the directory
                    onChooseCreate(folderName)

                    //close the dialog
                    hideDialog()
                }//onClick
            ) {
                Text("Create")
            }//TextButton
        },
        dismissButton = {
            TextButton(
                onClick = {hideDialog()}
            ) {
                Text("Cancel")
            }//TextButton
        }//dismissButton
    )//AlertDialog
}//fun