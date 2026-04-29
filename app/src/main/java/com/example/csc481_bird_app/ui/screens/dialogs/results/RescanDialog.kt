package com.example.csc481_bird_app.ui.screens.dialogs.results

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.csc481_bird_app.R

@Composable
fun RescanDialog (
    hideDialog: () -> Unit,
    onChooseRescan: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(painter = painterResource(id = R.drawable.outline_rescan_24), contentDescription = "Rescan Current Results")
        },
        title = {
            Text("Rescan")
        },
        text = {
            Text("Would you like to rescan the current results? This will create another save file.")
        },
        onDismissRequest = {
            hideDialog()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onChooseRescan()
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