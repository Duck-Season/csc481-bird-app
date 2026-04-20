package com.example.csc481_bird_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.csc481_bird_app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(
    onBack: () -> Unit
) {
    val faqs = listOf(
        "How do I scan a bird?" to "You can either take a photo with your camera or pick an existing image from your gallery using the buttons on the home screen.",
        "What if the bird is not identified?" to "The app uses a machine learning model to identify birds. If it fails, you can manually add a bird detection using the search box on the results screen.",
        "Where are my results saved?" to "Results are saved locally on your device. You can access previous scans using the 'Pick Image from Save File' button.",
        "How do I get more info about a bird?" to "On the results screen, each detected bird has a 'View on AllAboutBirds' button that will take you to a detailed guide."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(id = R.drawable.rounded_arrow_back_24),
                                contentDescription = "Back"
                            )
                        }
                        Text("FAQ")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            items(faqs) { faq ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = faq.first,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = faq.second,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
