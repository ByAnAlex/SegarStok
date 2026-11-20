package com.segarstok.ml.parser

import java.time.LocalDate

object DateParser {

    private val EXPIRY_KEYWORDS = listOf(
        "EXP", "EX", "ED", "USE BY", "BEST BEFORE", "BB",
        "KEDALUWARSA", "KADALUWARSA", "BAIK DIGUNAKAN SEBELUM"
    )

    private val PRODUCTION_KEYWORDS = listOf(
        "MFG", "MFD", "PROD", "MFG DATE", "TGL PRODUKSI", "TANGGAL PRODUKSI"
    )

    private val MONTH_MAP = mapOf(
        // Indonesian
        "JAN" to 1, "JANUARI" to 1,
        "FEB" to 2, "FEBRUARI" to 2,
        "MAR" to 3, "MARET" to 3,
        "APR" to 4, "APRIL" to 4,
        "MEI" to 5,
        "JUN" to 6, "JUNI" to 6,
        "JUL" to 7, "JULI" to 7,
        "AGU" to 8, "AGUSTUS" to 8,
        "SEP" to 9, "SEPTEMBER" to 9,
        "OKT" to 10, "OKTOBER" to 10,
        "NOV" to 11, "NOVEMBER" to 11,
        "DES" to 12, "DESEMBER" to 12,

        // English
        "JANUARY" to 1,
        "FEBRUARY" to 2,
        "MARCH" to 3,
        "MAY" to 5,
        "JUNE" to 6,
        "JULY" to 7,
        "AUG" to 8, "AUGUST" to 8,
        "OCT" to 10, "OCTOBER" to 10,
        "DEC" to 12, "DECEMBER" to 12
    )

    // Regex Patterns
    // 1. ISO: yyyy-MM-dd or yyyy/MM/dd
    private val PATTERN_ISO = Regex("""\b(20\d{2})[/-](0[1-9]|1[0-2])[/-](0[1-9]|[12]\d|3[01])\b""")

    // 2. Text month: dd MMM yyyy or dd MMMM yyyy or dd-MMM-yyyy or dd.MMM.yyyy
    // e.g., 25 DEC 2026, 25-DECEMBER-2026, 25.DES.2026, 25 DESEMBER 2026
    private val PATTERN_TEXT_MONTH = Regex(
        """\b(0?[1-9]|[12]\d|3[01])[\s/.\-]+([A-Za-z]{3,9})\.?[\s/.\-]+(20\d{2})\b""",
        RegexOption.IGNORE_CASE
    )

    // 3. Numeric 4-digit year: dd/MM/yyyy, dd-MM-yyyy, dd.MM.yyyy
    private val PATTERN_NUMERIC_4DIGIT = Regex("""\b(0?[1-9]|[12]\d|3[01])[/.\\-](0?[1-9]|1[0-2])[/.\\-](20\d{2})\b""")

    // 4. Numeric 2-digit year: dd/MM/yy, dd-MM-yy, dd.MM.yy
    private val PATTERN_NUMERIC_2DIGIT = Regex("""\b(0?[1-9]|[12]\d|3[01])[/.\\-](0?[1-9]|1[0-2])[/.\\-](\d{2})\b""")

    // 5. Typos pattern (e.g. 2S/12/2026, 25/I2/2026, 25/12/2O26)
    private val PATTERN_TYPO_DATE = Regex("""\b([0-9SlIOZB]{1,2})[/.\\-]([0-9SlIOZB]{1,2})[/.\\-]([0-9SlIOZB]{2,4})\b""")

    fun parse(ocrText: String): DateParseResult {
        if (ocrText.isBlank()) {
            return DateParseResult.NoCandidate
        }

        val lines = ocrText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val candidates = mutableListOf<DateCandidate>()

        for (i in lines.indices) {
            val line = lines[i]
            val context = detectContext(lines, i)
            val extracted = extractFromLine(line, context)
            candidates.addAll(extracted)
        }

        if (candidates.isEmpty()) {
            return DateParseResult.NoCandidate
        }

        // Deduplicate candidates with same date & context
        val deduplicated = mutableListOf<DateCandidate>()
        for (cand in candidates) {
            val existing = deduplicated.find { it.date == cand.date && it.context == cand.context }
            if (existing == null) {
                deduplicated.add(cand)
            } else {
                // Keep the candidate with higher confidence
                if (cand.confidence.ordinal < existing.confidence.ordinal) {
                    deduplicated.remove(existing)
                    deduplicated.add(cand)
                }
            }
        }

        // Check for ambiguous candidates (e.g. 03/04/2026 without explicit context)
        val ambiguousCandidate = deduplicated.find { it.warnings.any { w -> w.contains("ambigu", ignoreCase = true) } }
        if (ambiguousCandidate != null && deduplicated.all { it.context == DateContext.UNKNOWN }) {
            val reason = ambiguousCandidate.warnings.firstOrNull { it.contains("ambigu", ignoreCase = true) }
                ?: "Format tanggal ambigu (dapat dibaca dd/MM/yyyy atau MM/dd/yyyy)"
            return DateParseResult.Ambiguous(
                reason = reason,
                items = deduplicated
            )
        }

        // Sort candidates: EXPIRY > UNKNOWN > PRODUCTION, HIGH > MEDIUM > LOW
        val sorted = deduplicated.sortedWith(
            compareBy<DateCandidate> {
                when (it.context) {
                    DateContext.EXPIRY -> 0
                    DateContext.UNKNOWN -> 1
                    DateContext.PRODUCTION -> 2
                }
            }.thenBy { it.confidence.ordinal }
        )

        return DateParseResult.Candidates(sorted)
    }

    private fun detectContext(lines: List<String>, index: Int): DateContext {
        val currentLineUpper = lines[index].uppercase()
        val currentHasExpiry = EXPIRY_KEYWORDS.any { currentLineUpper.contains(it) }
        val currentHasProd = PRODUCTION_KEYWORDS.any { currentLineUpper.contains(it) }

        if (currentHasExpiry && !currentHasProd) return DateContext.EXPIRY
        if (currentHasProd && !currentHasExpiry) return DateContext.PRODUCTION

        // Check adjacent lines if current line doesn't have explicit single context
        val prevUpper = if (index > 0) lines[index - 1].uppercase() else ""
        val nextUpper = if (index < lines.size - 1) lines[index + 1].uppercase() else ""

        val adjacentHasExpiry = EXPIRY_KEYWORDS.any { prevUpper.contains(it) || nextUpper.contains(it) }
        val adjacentHasProd = PRODUCTION_KEYWORDS.any { prevUpper.contains(it) || nextUpper.contains(it) }

        return when {
            adjacentHasExpiry && !adjacentHasProd -> DateContext.EXPIRY
            adjacentHasProd && !adjacentHasExpiry -> DateContext.PRODUCTION
            else -> DateContext.UNKNOWN
        }
    }

    private fun extractFromLine(line: String, context: DateContext): List<DateCandidate> {
        val results = mutableListOf<DateCandidate>()
        val uppercaseLine = line.uppercase()

        // 1. Try ISO Pattern (yyyy-MM-dd or yyyy/MM/dd)
        PATTERN_ISO.findAll(line).forEach { match ->
            val year = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            val day = match.groupValues[3].toInt()
            tryCreateDate(year, month, day)?.let { date ->
                results.add(
                    DateCandidate(
                        date = date,
                        rawText = match.value,
                        matchedFormat = "yyyy-MM-dd",
                        context = context,
                        confidence = if (context != DateContext.UNKNOWN) CandidateConfidence.HIGH else CandidateConfidence.MEDIUM,
                        sourceLine = line
                    )
                )
            }
        }

        // 2. Try Text Month Pattern (dd MMM yyyy / dd MMMM yyyy)
        PATTERN_TEXT_MONTH.findAll(line).forEach { match ->
            val day = match.groupValues[1].toInt()
            val monthStr = match.groupValues[2].uppercase()
            val year = match.groupValues[3].toInt()

            val monthInt = MONTH_MAP[monthStr]
            if (monthInt != null) {
                tryCreateDate(year, monthInt, day)?.let { date ->
                    results.add(
                        DateCandidate(
                            date = date,
                            rawText = match.value,
                            matchedFormat = "dd MMM yyyy",
                            context = context,
                            confidence = if (context != DateContext.UNKNOWN) CandidateConfidence.HIGH else CandidateConfidence.MEDIUM,
                            sourceLine = line
                        )
                    )
                }
            }
        }

        // 3. Try Numeric 4-digit year Pattern (dd/MM/yyyy)
        PATTERN_NUMERIC_4DIGIT.findAll(line).forEach { match ->
            val first = match.groupValues[1].toInt()
            val second = match.groupValues[2].toInt()
            val year = match.groupValues[3].toInt()

            val isAmbiguous = first <= 12 && second <= 12 && first != second
            val day = first
            val month = second

            tryCreateDate(year, month, day)?.let { date ->
                val warnings = mutableListOf<String>()
                if (isAmbiguous) {
                    val firstStr = first.toString().padStart(2, '0')
                    val secondStr = second.toString().padStart(2, '0')
                    warnings.add("Format ${match.value} ambigu: dapat berarti ${firstStr}/${secondStr}/${year} (dd/MM/yyyy) atau ${secondStr}/${firstStr}/${year} (MM/dd/yyyy)")
                }

                val confidence = if (isAmbiguous && context == DateContext.UNKNOWN) {
                    CandidateConfidence.MEDIUM
                } else if (context != DateContext.UNKNOWN) {
                    CandidateConfidence.HIGH
                } else {
                    CandidateConfidence.MEDIUM
                }

                results.add(
                    DateCandidate(
                        date = date,
                        rawText = match.value,
                        matchedFormat = "dd/MM/yyyy",
                        context = context,
                        confidence = confidence,
                        sourceLine = line,
                        warnings = warnings
                    )
                )
            }
        }

        // 4. Try Numeric 2-digit year Pattern (dd/MM/yy)
        PATTERN_NUMERIC_2DIGIT.findAll(line).forEach { match ->
            val day = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            val year2 = match.groupValues[3].toInt()
            val year = 2000 + year2 // Pivot year 2000-2099

            // Avoid double matching if it's part of a 4-digit year
            val fullMatch = match.value
            if (!line.contains("${fullMatch}0") && !line.contains("${fullMatch}1") && !line.contains("${fullMatch}2")) {
                tryCreateDate(year, month, day)?.let { date ->
                    results.add(
                        DateCandidate(
                            date = date,
                            rawText = fullMatch,
                            matchedFormat = "dd/MM/yy",
                            context = context,
                            confidence = CandidateConfidence.MEDIUM,
                            sourceLine = line,
                            warnings = listOf("Tahun dua digit ($year2) diasumsikan $year")
                        )
                    )
                }
            }
        }

        // 5. OCR Typo Correction (if no valid candidate found from clean regexes)
        if (results.isEmpty()) {
            PATTERN_TYPO_DATE.findAll(line).forEach { match ->
                val rawToken = match.value
                val correctedToken = correctOcrTypos(rawToken)
                if (correctedToken != rawToken) {
                    // Try parsing corrected token as dd/MM/yyyy or dd/MM/yy
                    val parts = correctedToken.split('/', '-', '.')
                    if (parts.size == 3) {
                        val day = parts[0].toIntOrNull()
                        val month = parts[1].toIntOrNull()
                        var year = parts[2].toIntOrNull()

                        if (year != null && year < 100) {
                            year += 2000
                        }

                        if (day != null && month != null && year != null) {
                            tryCreateDate(year, month, day)?.let { date ->
                                results.add(
                                    DateCandidate(
                                        date = date,
                                        rawText = rawToken,
                                        matchedFormat = "OCR Corrected ($correctedToken)",
                                        context = context,
                                        confidence = CandidateConfidence.LOW,
                                        sourceLine = line,
                                        warnings = listOf("Koreksi OCR: '$rawToken' diubah menjadi '$correctedToken'")
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        return results
    }

    private fun correctOcrTypos(token: String): String {
        val sb = StringBuilder()
        for (ch in token) {
            when (ch) {
                'S', 's' -> sb.append('5')
                'O', 'o' -> sb.append('0')
                'I', 'l', 'i' -> sb.append('1')
                'Z', 'z' -> sb.append('2')
                'B' -> sb.append('8')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun tryCreateDate(year: Int, month: Int, day: Int): LocalDate? {
        return try {
            LocalDate.of(year, month, day)
        } catch (e: Exception) {
            null
        }
    }
}
