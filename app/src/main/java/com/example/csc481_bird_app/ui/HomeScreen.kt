package com.example.csc481_bird_app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.csc481_bird_app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onFileClick: () -> Unit
){
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("CSC481 Bird App")
                }//title
            )//TopAppBar
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onCameraClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.rounded_add_camera_24),
                    contentDescription = "File Icon"
                )//Icon
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Image with Camera")
            }
            Button(
                onClick = onGalleryClick
            ) {
                Row() {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_add_photo_alternate_24),
                        contentDescription = "File Icon"
                    )//Icon
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pick Image from Gallery")
                }//Row
            }
            Button(
                onClick = onFileClick
            ) {
                Row() {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_files_24),
                        contentDescription = "File Icon"
                    )//Icon
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pick Image from Save File")
                }//Row
            }
        }//Column
    }//Column
}//fun