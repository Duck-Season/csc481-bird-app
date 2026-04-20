package com.example.csc481_bird_app.detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap
import com.example.csc481_bird_app.utils.getLabels
import kotlin.math.exp

class YOLOv11Detector(private val context: Context) {
    private var interpreter: Interpreter? = null
    private var inputImageWidth = 1024
    private var inputImageHeight = 1024
    private val modelFilename = "YOLOv11_birdstraining_20jan2026_latest5.tflite"
    private val labelClasses: List<String>
    private val groupClasses: List<String>
    private val iouThreshold = 0.45f


    //initialize the interpreter and the lines
    init {
        //create the model interpreter
        try {
            val modelFile = loadModelFile()
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }//val
            interpreter = Interpreter(modelFile, options)

            //get the input shape of the model
            val inputShape = interpreter!!.getInputTensor(0).shape()
            inputImageHeight = inputShape[1]
            inputImageWidth = inputShape[2]
        } catch (e: Exception) {
            //something went wrong
            Log.e("csc481birdapp", "Error loading model: ${e.message}")
        }//try-catch

        //load in the labels from the text file
        labelClasses = getLabels(context)

        //get group class names ready for score aggregating
        val regex = Regex("\\s*\\(.*?\\)")
        groupClasses = labelClasses.map { it.replace(regex, "").trim() }
    }//init

    //loads the model into the interpreter
    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelFilename)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }//fun

    private fun preprocessImage(bitmap: Bitmap): Pair<ByteBuffer, Triple<Float, Float, Float>> {
        //track the initial dimensions of the image
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        //calculate smaller of two scales to preserve the image's aspect ratio
        val scale = minOf(
            inputImageWidth.toFloat() / originalWidth,
            inputImageHeight.toFloat() / originalHeight
        )//val

        //get scaled dimensions of the new image (which should be 640x640)
        val newWidth = (originalWidth * scale).toInt()
        val newHeight = (originalHeight * scale).toInt()

        //resize image to new dimensions
        val resizedBitmap = bitmap.scale(newWidth, newHeight, false)

        //YOLO needs a perfectly square image to work with
        //fill in empty space with gray color (114, 114, 114)
        val paddedBitmap = createBitmap(inputImageWidth, inputImageHeight)
        val canvas = Canvas(paddedBitmap)
        canvas.drawColor(android.graphics.Color.rgb(114, 114, 114))

        //center image on gray background and insert it
        val startX = (inputImageWidth - newWidth) / 2f
        val startY = (inputImageHeight - newHeight) / 2f
        canvas.drawBitmap(resizedBitmap, startX, startY, null)

        //convert the bitmap to ByteBuffer values for TFLite inferencing
        //first allocate memory for the buffer
        val byteBuffer = ByteBuffer.allocateDirect(inputImageWidth * inputImageHeight * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        //create an integer array for every pixel in the image
        val intValues = IntArray(inputImageWidth * inputImageHeight)
        paddedBitmap.getPixels(intValues, 0, inputImageWidth, 0, 0, inputImageWidth, inputImageHeight)

        for (pixelValue in intValues) {
            //bit shift to extract color values per pixel
            val r = (pixelValue shr 16) and 0xFF
            val g = (pixelValue shr 8) and 0xFF
            val b = pixelValue and 0xFF

            byteBuffer.putFloat(r / 255.0f)
            byteBuffer.putFloat(g / 255.0f)
            byteBuffer.putFloat(b / 255.0f)
        }//for

        //return the byteBuffer and scaleInfo as a tuple
        return Pair(byteBuffer, Triple(scale, startX, startY))
    }//fun

    fun detectObjects(bitmap: Bitmap,confidenceThreshold: Float = 0.25f): List<Detection> {
        //make sure the interpreter was created
        val interpreter = this.interpreter ?: return emptyList()

        try {
            //run the preprocessing function on the Image
            //obtain the inputBuffer for running interpreter
            //obtain how to "unscale" the image dimensions later for bounding boxes
            val (inputBuffer, scaleInfo) = preprocessImage(bitmap)
            val (scale, padX, padY) = scaleInfo

            //get the output tensor info
            val outputTensor = interpreter.getOutputTensor(0)
            val outputShape = outputTensor.shape() // e.g. [1, 204, 8400]
            val numElements = outputShape.reduce { a, b -> a * b }

            //allocate byteBuffer
            val outputBuffer = ByteBuffer.allocateDirect(4 * numElements)
            outputBuffer.order(ByteOrder.nativeOrder())

            //run the inference on the model (finally)
            interpreter.run(inputBuffer, outputBuffer)
            outputBuffer.rewind()

            //get the array
            val outputArray = Array(outputShape[1]) { FloatArray(outputShape[2]) }
            for (c in 0 until outputShape[1]) {
                for (i in 0 until outputShape[2]) {
                    outputArray[c][i] = outputBuffer.getFloat()
                }//for
            }//for

            //create the initial list of detections
            val detections = mutableListOf<Detection>()

            //aggregate the scores by grouping up species names
            val groupScores = mutableMapOf<String, Float>()

            //process each of the outputs
            for (i in 0 until outputShape[2]) {
                //clear the group scores beforehand
                groupScores.clear()

                val xCenter = outputArray[0][i]
                val yCenter = outputArray[1][i]
                val width = outputArray[2][i]
                val height = outputArray[3][i]

                //get the name of the best single candidate class
                var maxSingleConfidence = 0f
                var maxSingleClassIndex = 0

                var quickMax = 0f

                //remember to start counting from 4 onwards
                for (j in 4 until outputShape[1]) {
                    val classConfidence = outputArray[j][i]
                    val classIndex = j - 4

                    //check current single score against max
                    if (classConfidence > maxSingleConfidence) {
                        maxSingleConfidence = classConfidence
                        maxSingleClassIndex = j - 4
                    }//if

                    //quick pre-scan to skip classes with too low a threshold
                    if (outputArray[j][i] > quickMax) quickMax = outputArray[j][i]
                    if (quickMax < confidenceThreshold) continue

                    //update the group scores
                    val groupName = if (classIndex < labelClasses.size) groupClasses[classIndex] else continue
                    val current = groupScores.getOrDefault(groupName, 0f)
                    if (classConfidence > current) {
                        groupScores[groupName] = classConfidence
                    }//if
                }//for

                //get these for subdetections
                val groupScoresSorted = groupScores.entries
                    .sortedByDescending { it.value }
                val gSSentries = groupScoresSorted

                val subDetections = mutableListOf<Pair<String, Float>>()
                if (gSSentries.size > 1) subDetections.add(Pair(gSSentries[1].key, gSSentries[1].value))
                if (gSSentries.size > 2) subDetections.add(Pair(gSSentries[2].key, gSSentries[2].value))

                //if no group entry shows up, skip this one
                //otherwise, get the score from the highest group
                val bestGroup = groupScores.maxByOrNull { it.value } ?: continue
                val maxGroupConfidence = bestGroup.value

                //if the threshold is reached, begin creating the bounding box for our detection
                if (maxSingleConfidence >= confidenceThreshold) {
                    val x1 = (xCenter - width / 2) * inputImageWidth
                    val y1 = (yCenter - height / 2) * inputImageHeight
                    val w = width * inputImageWidth
                    val h = height * inputImageHeight

                    val originalX = (x1 - padX) / scale
                    val originalY = (y1 - padY) / scale
                    val originalW = w / scale
                    val originalH = h / scale

                    val clampedX = maxOf(0f, minOf(originalX, bitmap.width.toFloat()))
                    val clampedY = maxOf(0f, minOf(originalY, bitmap.height.toFloat()))
                    val clampedW = minOf(originalW, bitmap.width - clampedX)
                    val clampedH = minOf(originalH, bitmap.height - clampedY)

                    if (clampedW > 10 && clampedH > 10) {
                        //determine string from highest group
                        val className = if (maxSingleClassIndex < labelClasses.size) {
                            labelClasses[maxSingleClassIndex]
                        } else "Unknown Bird"

                        //add a new detection to the list
                        detections.add(
                            Detection(
                                bbox = RectF(clampedX, clampedY, clampedX + clampedW, clampedY + clampedH),
                                confidence = maxGroupConfidence,
                                classIndex = maxSingleClassIndex,
                                className = className,
                                subDetections = subDetections
                            )//Detection
                        )//.add
                    }//if
                }//if
            }//for

            //apply NMS to the initial list of detections to remove duplicates
            return applyNMS(detections, confidenceThreshold)
        } catch (e: Exception) {
            //something went wrong
            Log.e("csc481birdapp", "Error during detection: ${e.message}", e)
            return emptyList()
        }//try-catch
    }//fun

    //Non-Maximum Suppression (NMS) to remove duplicate boxes for an object
    //technically Soft-NMS, which instead of a hard cutoff between objects applies a score penalty based on overlap
    //https://www.abhik.ai/concepts/computer-vision/nms-soft-nms
    private fun applyNMS(detections: List<Detection>, scoreThreshold: Float): List<Detection> {
        //first filter the detections by whether they pass the threshold
        //then sort the detections by confidence score first
        val filteredDetections = detections
            .filter { it.confidence >= scoreThreshold }
            .sortedByDescending { it.confidence }
            .toMutableList()

        //make final results list
        val result = mutableListOf<Detection>()
        val sigma = 0.55f

        while (filteredDetections.isNotEmpty()) {
            //pop the highest scoring detection first from the list
            val best = filteredDetections.removeAt(0)

            if (best.confidence < scoreThreshold) break
            result.add(best)

            //decay scores of the remaining candidates based on IoU overlap with best
            for (det in filteredDetections) {
                val iou = calculateIoU(best.bbox, det.bbox)
                if(iou > iouThreshold){
                    det.confidence *= (1-iou)
                }else continue;
            }//for

            //remove any entries falling below score threshold
            filteredDetections.removeAll { it.confidence < scoreThreshold }
        }//while

        //result is a mutable list but the function returns an immutable
        return result
    }//fun

    //Intersection over Union (IoU) to measure overlap between two bounding boxes
    private fun calculateIoU(box1: RectF, box2: RectF): Float {
        //get the intersection of the boxes along several axes
        val intersectionLeft = maxOf(box1.left, box2.left)
        val intersectionTop = maxOf(box1.top, box2.top)
        val intersectionRight = minOf(box1.right, box2.right)
        val intersectionBottom = minOf(box1.bottom, box2.bottom)

        //check if the intersection actually makes sense first
        if (intersectionRight <= intersectionLeft || intersectionBottom <= intersectionTop) {
            return 0f
        }//if

        //calculate areas of the boxes, their intersection, and their area
        val box1Area = box1.width() * box1.height()
        val box2Area = box2.width() * box2.height()
        val intersectionArea = (intersectionRight - intersectionLeft) * (intersectionBottom - intersectionTop)
        val unionArea = box1Area + box2Area - intersectionArea

        //get the Intersection over Union
        //unless unionArea is already 0
        return if (unionArea > 0) intersectionArea / unionArea else 0f
    }//fun

    //need to close the interpreter to free up resources
    //this isn't done in here but will be necessary to call for the UI components
    fun close() {
        interpreter?.close()
    }//fun
}//class