package com.example.csc481_bird_app.ui.screens.results

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.csc481_bird_app.R
import com.example.csc481_bird_app.detector.Detection

@Composable
fun ResultsCard(
    det: Detection,
    bmp: Bitmap,
    index: Int,
    selectedIndex: Int,
    onSetSelectedIndex: () -> Unit,
    onLearnMoreClick: () -> Unit,
    isFavoriteSpecies: Boolean,
    setFavorite: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedIndex == index) {
                MaterialTheme.colorScheme.primaryFixedDim
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }//if-else
        ),
        onClick = {
            onSetSelectedIndex()
        }//onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Row() {
                //get width and height of bounding box; pick the smaller
                val bbW = det.bbox.right.toInt() - det.bbox.left.toInt()
                val bbH = det.bbox.bottom.toInt() - det.bbox.top.toInt()
                val bbS = minOf(bbW, bbH)

                //crop the image to the bounding box
                val previewBmp = createBitmap(
                    bmp,
                    det.bbox.left.toInt(),
                    det.bbox.top.toInt(),
                    bbS,
                    bbS,
                )//val

                AsyncImage(
                    model = previewBmp, // Get the URI/File instead of Bitmap
                    contentDescription = "Preview",
                    modifier = Modifier
                        .weight(0.25f)
                        .aspectRatio(1f / 1f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )//AsyncImage

                Spacer(
                    modifier = Modifier
                        .weight(0.05f)
                )//Spacer

                Text(
                    text = "${index + 1}.) ${det.className}",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .weight(0.5f),
                    fontWeight = FontWeight.Light,
                    style = TextStyle(
                        hyphens = Hyphens.Auto,
                        lineBreak = LineBreak.Paragraph
                    ),
                    color = MaterialTheme.colorScheme.primary
                )//Text

                Column(
                    modifier = Modifier
                        .weight(0.3f),
                    horizontalAlignment = Alignment.End
                ){
                    Text(
                        text = "${String.format("%.2f", det.confidence*100)}%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )//Text

                    Spacer(Modifier.padding(8.dp))

                    Icon(
                        painter = painterResource(
                            id = if (isFavoriteSpecies) {R.drawable.baseline_star_24} else {R.drawable.baseline_star_outline_24}
                        ),
                        tint = if (isFavoriteSpecies) Color.hsl(50.59f, 1f, 0.5f) else MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        modifier = Modifier
                            .clickable{
                                setFavorite()
                            }//.clickable
                    )//Icon
                }//Column
            }//Row

            //display when tapped
            if(selectedIndex == index){
                Spacer(modifier = Modifier.padding(4.dp))

                Column (){
                    Text(
                        text = "Other candidates",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )//Text

                    det.subDetections.forEachIndexed { index, subDet ->
                        Column (modifier = Modifier.fillMaxWidth()){
                            Row (
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = subDet.first,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(0.6f),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "${String.format("%.2f", subDet.second*100)}%",
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(0.4f),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }//Row
                            Row (
                                modifier = Modifier
                                    .fillMaxWidth()
                            )  {
                                LinearProgressIndicator(
                                    progress = { subDet.second },
                                )
                            }//Row
                        }//Column
                    }//forEach

                    Spacer(modifier = Modifier.padding(4.dp))

                    TextButton(
                        colors = ButtonDefaults.buttonColors(Color.Transparent, MaterialTheme.colorScheme.primary),
                        onClick = {
                            onLearnMoreClick()
                        }//onClick
                    ) {
                        Row() {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_open_in_browser_24),
                                contentDescription = "Learn more"
                            )//Icon
                            Text("Learn more about this species")
                        }//Row
                    }//TextButton
                }//Column
            }//if
        }//Column
    }//Card
}//Composable fun