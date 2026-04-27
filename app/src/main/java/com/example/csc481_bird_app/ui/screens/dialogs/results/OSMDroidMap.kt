package com.example.csc481_bird_app.ui.screens.dialogs.results

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmdroidMap(
    context: Context,
    modifier: Modifier = Modifier,
    startPoint: GeoPoint = GeoPoint(39.9523, -75.1638), // defaults to Philadelpha
    zoom: Double = 20.0
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView: MapView? = remember { null }

    //check shared preferences for osmdroid config
    //set them once
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = context.packageName
        }//.apply
    }//LaunchedEffect

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).also { map ->
                mapView = map
                map.setTileSource(TileSourceFactory.MAPNIK)
                map.setMultiTouchControls(true)
                map.controller.setZoom(zoom)
                map.controller.setCenter(startPoint)

               mapView.apply(){
                   val marker = Marker(this).apply {
                       position = startPoint
                       setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                       title = "Your location"
                       snippet = "${String.format("%.2f", startPoint.latitude)}, ${String.format("%.2f", startPoint.longitude)}"
                   }//.apply
                   overlays.add(marker)
               }//.apply
            }//MapView
        }//factory
    )//AndroidView

    // Tie osmdroid to the Compose lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> {}
            }//when
        }//val
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onDetach()
        }//onDispose
    }//DisposableEffect
}//fun