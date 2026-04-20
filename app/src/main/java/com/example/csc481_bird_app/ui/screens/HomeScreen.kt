package com.example.csc481_bird_app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
    onFileClick: () -> Unit,
    onFAQClick: () -> Unit,
    onToggleTheme: () -> Unit,
    isDark: Boolean
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
                },//title
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                }
            )//TopAppBar
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedButton(
                    onClick = onCameraClick,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .aspectRatio(1f/1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_add_camera_24),
                            contentDescription = "File Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )//Icon

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Take Image\nwith Camera",
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )//Text
                    }//Column
                }//Button
            }

            item {
                OutlinedButton(
                    onClick = onGalleryClick,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .aspectRatio(1f/1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_add_photo_alternate_24),
                            contentDescription = "File Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )//Icon
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pick Image\nfrom Gallery",
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }//Row
                }//Button
            }//item

            item {

                OutlinedButton(
                    onClick = onFileClick,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .aspectRatio(1f/1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_files_24),
                            contentDescription = "File Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )//Icon
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pick Image from\nSave File",
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }//Row
                }//Button
            }//item

            item {
                OutlinedButton(
                    onClick = onFAQClick,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .aspectRatio(1f/1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_more_vert_24),
                            contentDescription = "FAQ Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )//Icon
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Frequently Asked\nQuestions",
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }//item
        }//LazyVerticalGrid
    }//Column
}//fun