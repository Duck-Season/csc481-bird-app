package com.example.csc481_bird_app.ui.screens.dialogs.results

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import org.osmdroid.util.GeoPoint

@Composable
fun MapDialog(
    context: Context,
    hideDialog: () -> Unit,
    geoCoords: Pair<Float, Float>,
    locationName: String?
){
    //create the GeoPoint
    var geoPoint by remember { mutableStateOf(GeoPoint(geoCoords.first.toDouble(), geoCoords.second.toDouble())) }

    //the actual Composable
    AlertDialog(
        title = {
            Text("Map Preview")
        },
        text = {
            OsmdroidMap(
                modifier = Modifier.fillMaxWidth().height(500.dp).clip(RectangleShape),
                context = context,
                startPoint = geoPoint,
                locationName = locationName
            )
        },
        onDismissRequest = {
            hideDialog()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    //hide dialog
                    hideDialog()
                }//onClick
            ) {
                Text("Close Map")
            }//TextButton
        },
    )//AlertDialog
}//fun