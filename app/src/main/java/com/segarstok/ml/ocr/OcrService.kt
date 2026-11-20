package com.segarstok.ml.ocr

import android.content.Context
import android.net.Uri

interface OcrService {
    suspend fun processImage(imageUri: Uri, context: Context): Result<OcrResult>
}
