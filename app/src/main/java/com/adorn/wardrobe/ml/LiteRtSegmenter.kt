package com.adorn.wardrobe.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import androidx.palette.graphics.Palette
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

data class SegmentationResult(
    val cutoutPath: String,
    val originalPath: String,
    val dominantColorHex: String,
    val accentColorHex: String,
    val colorName: String
)

object LiteRtSegmenter {

    private const val CUTOUT_DIR_NAME = "wardrobe_cutouts"

    /**
     * Executes On-Device Background Removal and Color Extraction without cloud APIs.
     * Uses Google AI Edge LiteRT / ML Kit on-device segmentation model to cleanly isolate dresses,
     * sarees, lehengas, and jewelry from complex backgrounds (resorts, pools, grass, rooms),
     * with options to remove model face/head and bare arms.
     */
    suspend fun removeBackgroundAndExtractColors(
        context: Context,
        inputUriOrPath: String,
        removeFace: Boolean = true,
        removeArms: Boolean = true
    ): SegmentationResult = withContext(Dispatchers.Default) {

        // 1. Load Original Bitmap
        val originalBitmap = loadBitmap(context, inputUriOrPath)
            ?: throw IllegalArgumentException("Unable to decode input bitmap from: $inputUriOrPath")

        val originalPath = saveOriginalIfExternal(context, inputUriOrPath, originalBitmap)

        // 2. Perform Google On-Device Subject / Person Segmentation
        val maskBitmap = runMlKitSegmentation(originalBitmap)

        // 3. Apply Alpha Mask using PorterDuff DST_IN
        var cutoutBitmap = applyAlphaMask(originalBitmap, maskBitmap)

        // 4. Remove Face, Head & Neck, and Bare Arms if requested
        if (removeFace || removeArms) {
            cutoutBitmap = removeModelFaceAndSkin(originalBitmap, cutoutBitmap, removeFace, removeArms)
        }

        // 5. Extract Dominant & Accent Colors from pure clothing pixels
        val colorAnalysis = extractColorsFromCutout(cutoutBitmap)

        // 6. Save Lossless Cutout PNG into App-Private Storage
        val cutoutsDir = File(context.filesDir, CUTOUT_DIR_NAME).apply {
            if (!exists()) mkdirs()
        }
        val cutoutFileName = "cutout_${System.currentTimeMillis()}_${(100..999).random()}.png"
        val cutoutFile = File(cutoutsDir, cutoutFileName)

        FileOutputStream(cutoutFile).use { out ->
            cutoutBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        SegmentationResult(
            cutoutPath = cutoutFile.absolutePath,
            originalPath = originalPath,
            dominantColorHex = colorAnalysis.dominantColorHex,
            accentColorHex = colorAnalysis.accentColorHex,
            colorName = colorAnalysis.colorName
        )
    }

    private fun runMlKitSegmentation(original: Bitmap): Bitmap {
        return try {
            val options = SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                .enableRawSizeMask()
                .build()
            val segmenter = Segmentation.getClient(options)
            val inputImage = InputImage.fromBitmap(original, 0)

            val segmentationMask = Tasks.await(segmenter.process(inputImage))
            val maskBuffer = segmentationMask.buffer
            val maskWidth = segmentationMask.width
            val maskHeight = segmentationMask.height

            val maskBitmap = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888)
            val maskPixels = IntArray(maskWidth * maskHeight)

            maskBuffer.rewind()
            for (i in 0 until maskWidth * maskHeight) {
                val confidence = maskBuffer.float
                val alpha = when {
                    confidence >= 0.70f -> 255
                    confidence <= 0.30f -> 0
                    else -> (((confidence - 0.30f) / 0.40f) * 255).toInt().coerceIn(0, 255)
                }
                maskPixels[i] = Color.argb(alpha, 255, 255, 255)
            }
            maskBitmap.setPixels(maskPixels, 0, maskWidth, 0, 0, maskWidth, maskHeight)

            segmenter.close()

            if (maskWidth != original.width || maskHeight != original.height) {
                Bitmap.createScaledBitmap(maskBitmap, original.width, original.height, true)
            } else {
                maskBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: return full white mask if error
            Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.WHITE)
            }
        }
    }

    private fun removeModelFaceAndSkin(
        original: Bitmap,
        cutout: Bitmap,
        removeFace: Boolean,
        removeArms: Boolean
    ): Bitmap {
        val width = cutout.width
        val height = cutout.height

        val mutableCutout = cutout.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableCutout)

        var lowestCollarY = height.toFloat()

        // 1. Detect faces using on-device ML Kit Face Detection
        if (removeFace) {
            try {
                val detectorOptions = FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .build()
                val detector = FaceDetection.getClient(detectorOptions)
                val inputImage = InputImage.fromBitmap(original, 0)
                val faces = Tasks.await(detector.process(inputImage))

                val clearPaint = Paint().apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                    isAntiAlias = true
                }

                for (face in faces) {
                    val box = face.boundingBox
                    val faceW = box.width().toFloat()
                    val faceH = box.height().toFloat()
                    val chinY = box.bottom.toFloat()
                    val centerX = box.centerX().toFloat()

                    // Collar cutoff stops right under chin to keep blouse/neckline/collar intact
                    val collarY = (chinY + faceH * 0.08f).coerceAtMost(height.toFloat())
                    lowestCollarY = min(lowestCollarY, collarY)

                    val headHalfW = max(faceW * 0.95f, 25f)
                    val neckHalfW = max(faceW * 0.28f, 10f)

                    val path = Path().apply {
                        moveTo(centerX - neckHalfW, collarY)
                        quadTo(centerX, collarY + faceH * 0.04f, centerX + neckHalfW, collarY)
                        lineTo(min(width.toFloat(), centerX + headHalfW), chinY - faceH * 0.35f)
                        lineTo(min(width.toFloat(), centerX + headHalfW), 0f)
                        lineTo(max(0f, centerX - headHalfW), 0f)
                        lineTo(max(0f, centerX - headHalfW), chinY - faceH * 0.35f)
                        close()
                    }

                    canvas.drawPath(path, clearPaint)
                }
                detector.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Remove bare arms and exposed lateral skin
        if (removeArms) {
            val pixels = IntArray(width * height)
            mutableCutout.getPixels(pixels, 0, width, 0, 0, width, height)

            val leftArmBound = (width * 0.36f).toInt()
            val rightArmBound = (width * 0.64f).toInt()

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val idx = y * width + x
                    val pixel = pixels[idx]
                    val a = Color.alpha(pixel)
                    if (a > 40) {
                        val r = Color.red(pixel)
                        val g = Color.green(pixel)
                        val b = Color.blue(pixel)

                        val cb = 128.0 - 0.168736 * r - 0.331264 * g + 0.5 * b
                        val cr = 128.0 + 0.5 * r - 0.418688 * g - 0.081312 * b

                        val isSkin = cb in 75.0..135.0 && cr in 130.0..180.0 && r > g && g > b && (r - g) >= 8

                        if (isSkin && (x < leftArmBound || x > rightArmBound || y < lowestCollarY)) {
                            pixels[idx] = 0 // Transparent!
                        }
                    }
                }
            }
            mutableCutout.setPixels(pixels, 0, width, 0, 0, width, height)
        }

        return mutableCutout
    }

    private fun applyAlphaMask(original: Bitmap, mask: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Draw original
        canvas.drawBitmap(original, 0f, 0f, null)

        // Mask alpha into canvas
        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        canvas.drawBitmap(mask, 0f, 0f, maskPaint)

        return result
    }

    /**
     * Color extraction with AndroidX Palette ignoring transparent background pixels.
     */
    private fun extractColorsFromCutout(cutout: Bitmap): ColorAnalysisResult {
        val palette = Palette.from(cutout)
            .maximumColorCount(16)
            .generate()

        val dominantSwatch = palette.dominantSwatch
            ?: palette.vibrantSwatch
            ?: palette.mutedSwatch
            ?: palette.swatches.maxByOrNull { it.population }

        val accentSwatch = palette.vibrantSwatch
            ?: palette.lightVibrantSwatch
            ?: palette.darkVibrantSwatch
            ?: palette.mutedSwatch

        val domColorInt = dominantSwatch?.rgb ?: Color.BLACK
        val accColorInt = accentSwatch?.rgb ?: Color.WHITE

        val domHex = String.format("#%06X", 0xFFFFFF and domColorInt)
        val accHex = String.format("#%06X", 0xFFFFFF and accColorInt)

        val name = mapColorToHumanName(domColorInt)

        return ColorAnalysisResult(
            dominantColorHex = domHex,
            accentColorHex = accHex,
            colorName = name
        )
    }

    private data class ColorAnalysisResult(
        val dominantColorHex: String,
        val accentColorHex: String,
        val colorName: String
    )

    private fun mapColorToHumanName(colorInt: Int): String {
        val hsv = FloatArray(3)
        Color.colorToHSV(colorInt, hsv)
        val hue = hsv[0]
        val sat = hsv[1]
        val value = hsv[2]

        if (value < 0.18f) return "Noir Black"
        if (sat < 0.12f && value > 0.85f) return "Pure Ivory"
        if (sat < 0.15f) return "Platinum Gray"

        return when (hue) {
            in 0.0..15.0, in 345.0..360.0 -> if (value > 0.65f) "Crimson Red" else "Deep Maroon"
            in 15.0..45.0 -> if (value > 0.7f) "Champagne Gold" else "Copper Tan"
            in 45.0..68.0 -> "Royal Gold"
            in 68.0..160.0 -> if (value > 0.6f) "Emerald Green" else "Olive Forest"
            in 160.0..200.0 -> "Teal Peacock"
            in 200.0..260.0 -> if (value > 0.6f) "Sapphire Blue" else "Midnight Navy"
            in 260.0..315.0 -> if (value > 0.7f) "Royal Violet" else "Deep Plum"
            in 315.0..345.0 -> if (value > 0.75f) "Rose Gold" else "Blush Pink"
            else -> "Classic Elegance"
        }
    }

    private fun loadBitmap(context: Context, uriOrPath: String): Bitmap? {
        val trimmed = uriOrPath.trim()
        return try {
            if (trimmed.startsWith("/")) {
                BitmapFactory.decodeFile(trimmed)
            } else if (trimmed.startsWith("file://")) {
                val f = File(Uri.parse(trimmed).path ?: "")
                BitmapFactory.decodeFile(f.absolutePath)
            } else if (trimmed.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(trimmed))?.use {
                    BitmapFactory.decodeStream(it)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveOriginalIfExternal(context: Context, input: String, bitmap: Bitmap): String {
        if (input.startsWith(context.filesDir.absolutePath)) {
            return input
        }
        val origDir = File(context.filesDir, "wardrobe_images").apply {
            if (!exists()) mkdirs()
        }
        val origFile = File(origDir, "orig_${System.currentTimeMillis()}_${(100..999).random()}.jpg")
        FileOutputStream(origFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        return origFile.absolutePath
    }
}
