package com.example.csc481_bird_app.filesaving

import com.example.csc481_bird_app.detector.Detection

//class of object created when reading from a "saved scan" file
//a scan file will get deleted if image cannot be loaded from 'imgPath'
//geoCoords can be set to '0' or 'null' to say "location unknown"
data class SavedScan(
    val imgPath: String,                //path to the image file
    val geoCoords: Pair<Float?, Float?>,  //pair of decimal geolocation coordinates
    val detections: List<Detection>     //list of detections
)//data class