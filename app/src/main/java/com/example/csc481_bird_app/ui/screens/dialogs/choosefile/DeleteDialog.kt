package com.example.csc481_bird_app.ui.screens.dialogs.choosefile

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.csc481_bird_app.R

@Composable
fun DeleteDialog (
    hideDialog: () -> Unit,
    onChooseDelete: () -> Unit,
) {
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
        onDismissRequest = {hideDialog},
        confirmButton = {
            TextButton(
                onClick = {
                    //run delete function
                    onChooseDelete()

                    //hide dialog
                    hideDialog()
                }//onClick
            ) {
                Text("Yes")
            }//TextButton
        },
        dismissButton = {
            TextButton(
                onClick = {
                    hideDialog()
                }//onClick
            ) {
                Text("No")
            }//TextButton
        }//dismissButton
    )//AlertDialog
}//fun