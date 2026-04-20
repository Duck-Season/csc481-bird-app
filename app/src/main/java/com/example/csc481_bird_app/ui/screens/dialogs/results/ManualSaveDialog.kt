package com.example.csc481_bird_app.ui.screens.dialogs.results

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.csc481_bird_app.R

@Composable
fun ManualSaveDialog (
    hideDialog: () -> Unit,
    onChooseSave: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(painter = painterResource(id = R.drawable.rounded_save_24), contentDescription = "Save Current Scan")
        },
        title = {
            Text("Save File?")
        },
        text = {
            Text("Are you sure you want to manually save this scan?")
        },
        onDismissRequest = {
            hideDialog
        },
        confirmButton = {
            TextButton(
                onClick = {
                    //run saving function
                    onChooseSave()

                    //hide dialog
                    hideDialog()
                }//onClick
            ) {
                Text("Save Scan")
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