package com.segarstok.ml.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MlKitOcrService : OcrService {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    override suspend fun processImage(imageUri: Any, context: Context?): Result<OcrResult> {
        if (context == null) {
            return Result.failure(IllegalArgumentException("Context tidak boleh null untuk pemrosesan ML Kit"))
        }
        return try {
            val inputImage = when (imageUri) {
                is Uri -> InputImage.fromFilePath(context, imageUri)
                is String -> InputImage.fromFilePath(context, Uri.parse(imageUri))
                else -> return Result.failure(IllegalArgumentException("Format gambar tidak didukung"))
            }

            suspendCancellableCoroutine { continuation ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val blocks = visionText.textBlocks.map { block ->
                            OcrTextBlock(
                                text = block.text,
                                lines = block.lines.map { line -> OcrTextLine(line.text) }
                            )
                        }
                        val ocrResult = OcrResult(
                            fullText = visionText.text,
                            blocks = blocks
                        )
                        if (continuation.isActive) {
                            continuation.resume(Result.success(ocrResult))
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(exception))
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
