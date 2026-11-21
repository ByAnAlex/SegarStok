package com.segarstok.ml.ocr

import android.content.Context

interface OcrService {
    suspend fun processImage(imageUri: Any, context: Context? = null): Result<OcrResult>
}
