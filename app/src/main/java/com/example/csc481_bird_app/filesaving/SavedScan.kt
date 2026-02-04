package com.example.csc481_bird_app.filesaving

//class of object created when reading/writing to a "saved scan" file
data class SavedScan(
    val imgPath: String,                //path to the image file
    val boxCoords: FloatArray,          //bounding box where the object is
    val confidence: Float,              //confidence in classification
    val classIndex: Int,                //numbered index of object class
    val className: String,              //string index of object class
    val geoCoords: Pair<Float, Float>   //pair of decimal geolocation coordinates
)
