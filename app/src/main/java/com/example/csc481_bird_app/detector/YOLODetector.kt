package com.example.csc481_bird_app.detector

import android.content.Context
import android.graphics.Bitmap
import com.example.csc481_bird_app.data.Detection
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class YoloV11Detector(
    private val context: Context,
    private val modelPath: String = "YOLOv11_birds.tflite",
    private val labelPath: String = "labels.txt"
) {
    private var interpreter: Interpreter? = null
    private var labels: List<String> = listOf()
    private val inputSize = 640 // YOLOv11 default input size

    //called when class gets initialized
    init {
        setupInterpreter()
        loadLabels()
    }//init

    private fun setupInterpreter() {
        val model = FileUtil.loadMappedFile(context, modelPath)
        val options = Interpreter.Options().apply {
            setNumThreads(4)
            addDelegate(GpuDelegate())
        }//val
        interpreter = Interpreter(model, options)
    }//fun

    private fun loadLabels() {
        labels = FileUtil.loadLabels(context, labelPath)
    }//fun

    fun detect(bitmap: Bitmap, confidenceThreshold: Float = 0.5f): List<Detection> {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

        // Output shape depends on YOLOv11 model (typically [1, 84, 8400] for COCO)
        val outputShape = interpreter?.getOutputTensor(0)?.shape()
        val output = Array(1) { Array(outputShape?.get(1) ?: 204) { FloatArray(outputShape?.get(2) ?: 8400) } }

        interpreter?.run(inputBuffer, output)

        return parseOutput(output[0], confidenceThreshold, bitmap.width, bitmap.height)
    }//fun

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in pixels) {
            // Normalize to [0, 1]
            byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
            byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixel and 0xFF) / 255.0f)
        }//for

        return byteBuffer
    }//fun

    private fun parseOutput(
        output: Array<FloatArray>,
        threshold: Float,
        originalWidth: Int,
        originalHeight: Int
    ): List<Detection> {
        val detections = mutableListOf<Detection>()
        val numClasses = output.size - 4

        for (i in 0 until output[0].size) {
            val x = output[0][i]
            val y = output[1][i]
            val w = output[2][i]
            val h = output[3][i]

            var maxScore = 0f
            var classId = 0

            for (c in 0 until numClasses) {
                val score = output[4 + c][i]
                if (score > maxScore) {
                    maxScore = score
                    classId = c
                }//if
            }//for

            if (maxScore > threshold) {
                val scaleX = originalWidth / inputSize.toFloat()
                val scaleY = originalHeight / inputSize.toFloat()

                detections.add(Detection(
                    bbox = floatArrayOf(x * scaleX, y * scaleY, w * scaleX, h * scaleY),
                    score = maxScore,
                    classId = classId,
                    label = if (classId < labels.size) labels[classId] else "Unknown"
                ))
            }//if
        }//for

        return applyNMS(detections)
    }//fun

    private fun applyNMS(detections: List<Detection>, iouThreshold: Float = 0.45f): List<Detection> {
        // Non-maximum suppression implementation
        // Sort by score and remove overlapping boxes
        val sorted = detections.sortedByDescending { it.score }
        val selected = mutableListOf<Detection>()

        for (detection in sorted) {
            var keep = true
            for (selected_detection in selected) {
                if (iou(detection.bbox, selected_detection.bbox) > iouThreshold) {
                    keep = false
                    break
                }//if
            }//for

            if (keep) selected.add(detection)
        }//for

        return selected
    }//fun

    private fun iou(box1: FloatArray, box2: FloatArray): Float {
        val x1 = maxOf(box1[0] - box1[2] / 2, box2[0] - box2[2] / 2)
        val y1 = maxOf(box1[1] - box1[3] / 2, box2[1] - box2[3] / 2)
        val x2 = minOf(box1[0] + box1[2] / 2, box2[0] + box2[2] / 2)
        val y2 = minOf(box1[1] + box1[3] / 2, box2[1] + box2[3] / 2)

        val intersection = maxOf(0f, x2 - x1) * maxOf(0f, y2 - y1)
        val area1 = box1[2] * box1[3]
        val area2 = box2[2] * box2[3]
        val union = area1 + area2 - intersection

        return if (union > 0) intersection / union else 0f
    }//fun

    fun close() {
        //close the interpreter
        interpreter?.close()
    }//fun
}//class