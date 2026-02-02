package com.example.csc481_bird_app.data

data class Detection(
    val bbox: FloatArray, // [x, y, width, height]
    val score: Float,
    val classId: Int,
    val label: String
)
