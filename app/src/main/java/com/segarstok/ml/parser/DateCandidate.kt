package com.segarstok.ml.parser

import java.time.LocalDate

enum class DateContext {
    EXPIRY,
    PRODUCTION,
    UNKNOWN
}

enum class CandidateConfidence {
    HIGH,
    MEDIUM,
    LOW
}

data class DateCandidate(
    val date: LocalDate,
    val rawText: String,
    val matchedFormat: String,
    val context: DateContext,
    val confidence: CandidateConfidence,
    val sourceLine: String,
    val warnings: List<String> = emptyList()
)

sealed interface DateParseResult {
    data object NoCandidate : DateParseResult
    data class Candidates(val items: List<DateCandidate>) : DateParseResult
    data class Ambiguous(
        val reason: String,
        val items: List<DateCandidate>
    ) : DateParseResult
}
