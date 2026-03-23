package com.example.csc481_bird_app.detector

import android.graphics.RectF

//class of object outputted when the model detects something
data class Detection(
    val bbox: RectF,        //bounding box where the object is
    var confidence: Float,  //confidence in classification
    val classIndex: Int,    //numbered index of object class
    val className: String,   //string index of object class

    //two subDetections per detection entry
    //String is className
    //Float is confidence score
    val subDetections: List<Pair<String, Float>>
)//data class
