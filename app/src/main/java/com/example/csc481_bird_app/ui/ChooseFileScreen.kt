package com.example.csc481_bird_app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.csc481_bird_app.detector.DectectionsViewModel

@Composable
fun ChooseFileScreen(
    viewModel: DectectionsViewModel,
    onDetectionsComplete: () -> Unit,
    onBack: () -> Unit
){

    val context = LocalContext.current
    val listSaves = context.filesDir.listFiles()


    //mutable values
    var isLoading by remember { mutableStateOf<Boolean>(false) }

    //Column to hold everything in place
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

    }//Column
}//fun