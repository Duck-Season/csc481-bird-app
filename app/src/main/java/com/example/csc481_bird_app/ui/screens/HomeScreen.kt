package com.example.csc481_bird_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = onFileClick,
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_files_24),
                            contentDescription = "Collections"
                        )
                    },
                    label = { Text("Collections") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onCameraClick,
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_add_camera_24),
                            contentDescription = "Camera"
                        )
                    },
                    label = { Text("Camera") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onGalleryClick,
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_add_photo_alternate_24),
                            contentDescription = "Gallery"
                        )
                    },
                    label = { Text("Gallery") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to the Bird App!\n\nUse the navigation bar below to get started.",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(24.dp)
            )
        }//Column
    }
}
