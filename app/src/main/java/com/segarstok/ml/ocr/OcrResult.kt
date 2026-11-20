package com.segarstok.ml.ocr

data class OcrTextLine(
    val text: String
)

data class OcrTextBlock(
    val text: String,
    val lines: List<OcrTextLine> = emptyList()
)

data class OcrResult(
    val fullText: String,
    val blocks: List<OcrTextBlock> = emptyList()
)
