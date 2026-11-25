package com.segarstok.ml.parser

import java.time.LocalDate

object DateParser {

    private val EXPIRY_KEYWORDS = listOf(
        "EXP", "EX", "ED", "USE BY", "BEST BEFORE", "BB", "BBE",
        "FXP", "EVP", "EXF", "ERF", "EXR", "EXPIRATION", "EXP DATE",
        "KEDALUWARSA", "KADALUWARSA", "BAIK DIGUNAKAN SEBELUM"
    )

    private val PRODUCTION_KEYWORDS = listOf(
        "MFG", "MFD", "PROD", "PRD", "NFG", "HFG", "WFG", "MPG", "MFC", "MFO", "MF6",
        "NFC", "NFD", "PFG", "PEG", "MFG DATE", "TGL PRODUKSI", "TANGGAL PRODUKSI", "PRODUKSI"
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

    // 5. Space-separated: dd MM yyyy or dd MM yy (e.g. 11 05 21 or 11 05 2021)
    private val PATTERN_SPACE_SEPARATED = Regex("""(?<!\d)(0?[1-9]|[12]\d|3[01])\s+(0?[1-9]|1[0-2])\s+(20\d{2}|\d{2})(?!\d)""")

    // 6. Continuous 8-digit: ddMMyyyy or yyyyMMdd (e.g. 11052021 or 20210511)
    private val PATTERN_CONTINUOUS_8DIGIT_DDMMYYYY = Regex("""(?<!\d)(0[1-9]|[12]\d|3[01])(0[1-9]|1[0-2])(20\d{2})(?!\d)""")
    private val PATTERN_CONTINUOUS_8DIGIT_YYYYMMDD = Regex("""(?<!\d)(20\d{2})(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])(?!\d)""")

    // 7. Continuous 6-digit: ddMMyy (e.g. 110521, 060521 in MFG:060521 or EXP:110521)
    private val PATTERN_CONTINUOUS_6DIGIT_DDMMYY = Regex("""(?<!\d)(0[1-9]|[12]\d|3[01])(0[1-9]|1[0-2])(\d{2})(?!\d)""")

    // 8. Typos pattern
    private val PATTERN_TYPO_DATE = Regex("""\b([0-9SlIOZB]{1,2})[/.\\-]([0-9SlIOZB]{1,2})[/.\\-]([0-9SlIOZB]{2,4})\b""")
    private val PATTERN_TYPO_CONTINUOUS = Regex("""(?<![0-9A-Za-z])([0-9SlIOZB]{6}|[0-9SlIOZB]{8})(?![0-9A-Za-z])""")

    fun parse(ocrText: String): DateParseResult {
        if (ocrText.isBlank()) {
            return DateParseResult.NoCandidate
        }

        val lines = ocrText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val candidates = mutableListOf<DateCandidate>()

        val normalizedLines = lines.map { normalizeDotMatrixLine(it) }

        for (i in lines.indices) {
            val line = lines[i]
            val normalized = normalizedLines[i]

            val context = detectContext(lines, normalizedLines, i)
            val extracted = extractFromLine(line, context)
            candidates.addAll(extracted)

            if (normalized != line) {
                val extractedNormalized = extractFromLine(normalized, context)
                candidates.addAll(extractedNormalized)
            }
        }

        if (candidates.isEmpty()) {
            return DateParseResult.NoCandidate
        }

        // Smart Pair Resolution for Manufacturing vs Expiry dates when two distinct dates exist
        val distinctDates = candidates.map { it.date }.distinct()
        if (distinctDates.size == 2) {
            val sortedDates = distinctDates.sorted()
            val earlierDate = sortedDates[0]
            val laterDate = sortedDates[1]

            // In food/beverage packaging, when 2 distinct dates are present on the same product item:
            // The earlier date MUST BE Production (MFG), and the later date MUST BE Expiry (EXP).
            for (i in candidates.indices) {
                val cand = candidates[i]
                if (cand.date == earlierDate) {
                    candidates[i] = cand.copy(
                        context = DateContext.PRODUCTION,
                        confidence = CandidateConfidence.HIGH,
                        warnings = cand.warnings.filterNot { it.contains("Kedaluwarsa", ignoreCase = true) } +
                                "Diidentifikasi sebagai Tanggal Produksi (tanggal lebih awal dalam pasangan)"
                    )
                } else if (cand.date == laterDate) {
                    candidates[i] = cand.copy(
                        context = DateContext.EXPIRY,
                        confidence = CandidateConfidence.HIGH,
                        warnings = cand.warnings.filterNot { it.contains("Produksi", ignoreCase = true) } +
                                "Diidentifikasi sebagai Tanggal Kedaluwarsa (tanggal lebih akhir dalam pasangan)"
                    )
                }
            }
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

    private fun detectContext(lines: List<String>, normalizedLines: List<String>, index: Int): DateContext {
        val currentLineUpper = lines[index].uppercase()
        val currentNormUpper = normalizedLines[index].uppercase()

        val currentHasExpiry = EXPIRY_KEYWORDS.any { currentLineUpper.contains(it) || currentNormUpper.contains(it) }
        val currentHasProd = PRODUCTION_KEYWORDS.any { currentLineUpper.contains(it) || currentNormUpper.contains(it) }

        if (currentHasExpiry && !currentHasProd) return DateContext.EXPIRY
        if (currentHasProd && !currentHasExpiry) return DateContext.PRODUCTION

        // Check adjacent lines ONLY if the adjacent line is a label line (does not contain its own date numbers)
        val hasDigitsPattern = Regex("""\d{4,8}|\d{2}[/.\\-]\d{2}[/.\\-]\d{2,4}""")

        val prevLine = if (index > 0) lines[index - 1] else ""
        val nextLine = if (index < lines.size - 1) lines[index + 1] else ""

        val prevHasDateDigits = hasDigitsPattern.containsMatchIn(prevLine)
        val nextHasDateDigits = hasDigitsPattern.containsMatchIn(nextLine)

        val prevUpper = if (index > 0 && !prevHasDateDigits) lines[index - 1].uppercase() + " " + normalizedLines[index - 1].uppercase() else ""
        val nextUpper = if (index < lines.size - 1 && !nextHasDateDigits) lines[index + 1].uppercase() + " " + normalizedLines[index + 1].uppercase() else ""

        val adjacentHasExpiry = EXPIRY_KEYWORDS.any { (prevUpper.isNotEmpty() && prevUpper.contains(it)) || (nextUpper.isNotEmpty() && nextUpper.contains(it)) }
        val adjacentHasProd = PRODUCTION_KEYWORDS.any { (prevUpper.isNotEmpty() && prevUpper.contains(it)) || (nextUpper.isNotEmpty() && nextUpper.contains(it)) }

        return when {
            adjacentHasExpiry && !adjacentHasProd -> DateContext.EXPIRY
            adjacentHasProd && !adjacentHasExpiry -> DateContext.PRODUCTION
            else -> DateContext.UNKNOWN
        }
    }

    private fun normalizeDotMatrixLine(line: String): String {
        val singleCharCollapsed = line.replace(Regex("""(?<=\b[A-Za-z0-9])\s+(?=[A-Za-z0-9]\b)"""), "")
        return singleCharCollapsed.replace(Regex("""([A-Za-z]{2,5})[:;.\s]+(?=\d)""")) { match ->
            "${match.groupValues[1]}:"
        }
    }

    private fun extractFromLine(line: String, context: DateContext): List<DateCandidate> {
        val results = mutableListOf<DateCandidate>()

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

        // 5. Try Space-separated Pattern (dd MM yyyy / dd MM yy)
        PATTERN_SPACE_SEPARATED.findAll(line).forEach { match ->
            val day = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            var year = match.groupValues[3].toInt()
            val isTwoDigitYear = match.groupValues[3].length == 2
            if (isTwoDigitYear) year += 2000

            tryCreateDate(year, month, day)?.let { date ->
                val warnings = if (isTwoDigitYear) listOf("Tahun dua digit diasumsikan $year") else emptyList()
                results.add(
                    DateCandidate(
                        date = date,
                        rawText = match.value,
                        matchedFormat = if (isTwoDigitYear) "dd MM yy" else "dd MM yyyy",
                        context = context,
                        confidence = if (context != DateContext.UNKNOWN) CandidateConfidence.HIGH else CandidateConfidence.MEDIUM,
                        sourceLine = line,
                        warnings = warnings
                    )
                )
            }
        }

        // 6. Try Continuous 8-digit Pattern (ddMMyyyy / yyyyMMdd)
        PATTERN_CONTINUOUS_8DIGIT_DDMMYYYY.findAll(line).forEach { match ->
            val day = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            val year = match.groupValues[3].toInt()

            tryCreateDate(year, month, day)?.let { date ->
                results.add(
                    DateCandidate(
                        date = date,
                        rawText = match.value,
                        matchedFormat = "ddMMyyyy",
                        context = context,
                        confidence = if (context != DateContext.UNKNOWN) CandidateConfidence.HIGH else CandidateConfidence.MEDIUM,
                        sourceLine = line
                    )
                )
            }
        }

        PATTERN_CONTINUOUS_8DIGIT_YYYYMMDD.findAll(line).forEach { match ->
            val year = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            val day = match.groupValues[3].toInt()

            tryCreateDate(year, month, day)?.let { date ->
                results.add(
                    DateCandidate(
                        date = date,
                        rawText = match.value,
                        matchedFormat = "yyyyMMdd",
                        context = context,
                        confidence = if (context != DateContext.UNKNOWN) CandidateConfidence.HIGH else CandidateConfidence.MEDIUM,
                        sourceLine = line
                    )
                )
            }
        }

        // 7. Try Continuous 6-digit Pattern (ddMMyy, e.g. 110521, 060521)
        PATTERN_CONTINUOUS_6DIGIT_DDMMYY.findAll(line).forEach { match ->
            val day = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            val year2 = match.groupValues[3].toInt()
            val year = 2000 + year2

            tryCreateDate(year, month, day)?.let { date ->
                results.add(
                    DateCandidate(
                        date = date,
                        rawText = match.value,
                        matchedFormat = "ddMMyy",
                        context = context,
                        confidence = if (context != DateContext.UNKNOWN) CandidateConfidence.HIGH else CandidateConfidence.MEDIUM,
                        sourceLine = line,
                        warnings = listOf("Format 6 digit tanpa pemisah (${match.value}) diasumsikan $day/$month/$year")
                    )
                )
            }
        }

        // 8. OCR Typo Correction (if no valid candidate found from clean regexes)
        if (results.isEmpty()) {
            PATTERN_TYPO_DATE.findAll(line).forEach { match ->
                val rawToken = match.value
                val correctedToken = correctOcrTypos(rawToken)
                if (correctedToken != rawToken) {
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

            PATTERN_TYPO_CONTINUOUS.findAll(line).forEach { match ->
                val rawToken = match.value
                val correctedToken = correctOcrTypos(rawToken)
                if (correctedToken != rawToken && correctedToken.all { it.isDigit() }) {
                    if (correctedToken.length == 6) {
                        val day = correctedToken.substring(0, 2).toIntOrNull()
                        val month = correctedToken.substring(2, 4).toIntOrNull()
                        val year2 = correctedToken.substring(4, 6).toIntOrNull()
                        if (day != null && month != null && year2 != null) {
                            val year = 2000 + year2
                            tryCreateDate(year, month, day)?.let { date ->
                                results.add(
                                    DateCandidate(
                                        date = date,
                                        rawText = rawToken,
                                        matchedFormat = "OCR Corrected ($correctedToken)",
                                        context = context,
                                        confidence = CandidateConfidence.LOW,
                                        sourceLine = line,
                                        warnings = listOf("Koreksi OCR: '$rawToken' diubah menjadi '$correctedToken' ($day/$month/$year)")
                                    )
                                )
                            }
                        }
                    } else if (correctedToken.length == 8) {
                        val day = correctedToken.substring(0, 2).toIntOrNull()
                        val month = correctedToken.substring(2, 4).toIntOrNull()
                        val year = correctedToken.substring(4, 8).toIntOrNull()
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
                                        warnings = listOf("Koreksi OCR: '$rawToken' diubah menjadi '$correctedToken' ($day/$month/$year)")
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
