package com.example.csc481_bird_app.ui.screens.choosefile

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.csc481_bird_app.R
import java.io.File

@Composable
fun FileCard(
    folder: File? = null,
    fileName: String,
    imgUri: Uri? = null,
    isCameraSave: Boolean = false,
    isDeleteEnabled: Boolean = true,
    onSelection: () -> Unit,
    onDelete: () -> Unit = {},
    onMove: () -> Unit = {},
){
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        onClick = {
            onSelection()
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ){
            if(folder != null){
                val isEmptyFolder = folder.listFiles()!!.size == 0

                Icon(
                    painter = painterResource(if (isEmptyFolder) R.drawable.outline_folder_open_24 else R.drawable.baseline_folder_24),
                    contentDescription = "Folder named ${fileName}",
                    modifier = Modifier
                        .size(48.dp)
                )//Icon
            }else{
                //a preview of the image is more intuitive than a date
                AsyncImage(
                    model = imgUri, // Get the URI/File instead of Bitmap
                    contentDescription = "Preview",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )//AsyncImage
            }//if-else

            Spacer(
                modifier = Modifier
                    .weight(0.1f)
            )//Spacer

            //use the readable time name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            ){
                //only show for saved scans
                if(folder == null){
                    Icon(
                        painter = painterResource(id = if(isCameraSave) R.drawable.rounded_add_camera_24 else R.drawable.rounded_add_photo_alternate_24),
                        contentDescription = "Save taken with phone " + if(isCameraSave) "camera" else "gallery",
                        modifier = Modifier
                            .size(32.dp)
                    )//Icon
                }//if
                Text(
                    text = fileName,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(0.4f))

                Box(){
                    IconButton(
                        onClick = {expanded = !expanded}
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_more_vert_24),
                            contentDescription = "More Options Icon",
                            modifier = Modifier
                                .size(32.dp),
                        )//Icon
                    }//IconButton

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        //all folders will be kept at the top level for now
                        //that is, you cannot make folders within folders happen
                        if(folder == null){
                            DropdownMenuItem(
                                text = { Text("Move File") },
                                onClick = {
                                    onMove()
                                }//onClick
                            )//DropdownMenuItem
                        }//if

                        //can delete files and folders (for folders, check no files are inside first)
                        val deleteStr = if(isDeleteEnabled){
                            "Delete ${if(folder != null) "Folder" else "File"}"
                        }else{
                            "Cannot delete (files inside)"
                        }//val if-else

                        DropdownMenuItem(
                            text = { Text(deleteStr) },
                            enabled = isDeleteEnabled,
                            onClick = {
                                onDelete()
                            }//onClick
                        )//DropdownMenuItem
                    }//DropdownMenu
                }//Box
            }//Row
        }//Row
    }//Card
}//fun