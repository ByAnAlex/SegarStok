package com.segarstok.ml.parser

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DateParserTest {

    @Test
    fun test1_exp_ddMMyyyy() {
        val result = DateParser.parse("EXP 25/12/2026")
        assertTrue(result is DateParseResult.Candidates)
        val candidates = (result as DateParseResult.Candidates).items
        assertEquals(1, candidates.size)
        val cand = candidates[0]
        assertEquals(LocalDate.of(2026, 12, 25), cand.date)
        assertEquals(DateContext.EXPIRY, cand.context)
        assertEquals(CandidateConfidence.HIGH, cand.confidence)
    }

    @Test
    fun test2_ed_ddMMyyyy() {
        val result = DateParser.parse("ED: 25-12-2026")
        assertTrue(result is DateParseResult.Candidates)
        val cand = (result as DateParseResult.Candidates).items[0]
        assertEquals(LocalDate.of(2026, 12, 25), cand.date)
        assertEquals(DateContext.EXPIRY, cand.context)
        assertEquals(CandidateConfidence.HIGH, cand.confidence)
    }

    @Test
    fun test3_bestBefore_ddMMMyyyy() {
        val result = DateParser.parse("Best Before 25 DEC 2026")
        assertTrue(result is DateParseResult.Candidates)
        val cand = (result as DateParseResult.Candidates).items[0]
        assertEquals(LocalDate.of(2026, 12, 25), cand.date)
        assertEquals(DateContext.EXPIRY, cand.context)
        assertEquals(CandidateConfidence.HIGH, cand.confidence)
    }

    @Test
    fun test4_baikDigunakanSebelum_indonesian() {
        val result = DateParser.parse("Baik digunakan sebelum 25 Desember 2026")
        assertTrue(result is DateParseResult.Candidates)
        val cand = (result as DateParseResult.Candidates).items[0]
        assertEquals(LocalDate.of(2026, 12, 25), cand.date)
        assertEquals(DateContext.EXPIRY, cand.context)
        assertEquals(CandidateConfidence.HIGH, cand.confidence)
    }

    @Test
    fun test5_mfg_productionDate() {
        val result = DateParser.parse("MFG 01/10/2026")
        assertTrue(result is DateParseResult.Candidates)
        val cand = (result as DateParseResult.Candidates).items[0]
        assertEquals(LocalDate.of(2026, 10, 1), cand.date)
        assertEquals(DateContext.PRODUCTION, cand.context)
    }

    @Test
    fun test6_mfgAndExp_both() {
        val input = "MFG 01/10/2026\nEXP 01/10/2027"
        val result = DateParser.parse(input)
        assertTrue(result is DateParseResult.Candidates)
        val items = (result as DateParseResult.Candidates).items
        assertEquals(2, items.size)

        val expiryCand = items.find { it.context == DateContext.EXPIRY }
        val prodCand = items.find { it.context == DateContext.PRODUCTION }

        assertNotNull(expiryCand)
        assertNotNull(prodCand)

        assertEquals(LocalDate.of(2027, 10, 1), expiryCand!!.date)
        assertEquals(LocalDate.of(2026, 10, 1), prodCand!!.date)

        // Verify Expiry is ranked higher than Production
        assertEquals(DateContext.EXPIRY, items[0].context)
    }

    @Test
    fun test7_ambiguousWithoutLabel() {
        val result = DateParser.parse("03/04/2026")
        assertTrue("Expected Ambiguous result for 03/04/2026", result is DateParseResult.Ambiguous)
        val amb = result as DateParseResult.Ambiguous
        assertTrue(amb.reason.contains("ambigu", ignoreCase = true))
        assertFalse(amb.items.isEmpty())
        assertEquals(LocalDate.of(2026, 4, 3), amb.items[0].date)
    }

    @Test
    fun test8_isoFormat() {
        val result = DateParser.parse("2026-12-25")
        assertTrue(result is DateParseResult.Candidates)
        val cand = (result as DateParseResult.Candidates).items[0]
        assertEquals(LocalDate.of(2026, 12, 25), cand.date)
        assertEquals("yyyy-MM-dd", cand.matchedFormat)
    }

    @Test
    fun test9_invalidDate_feb31() {
        val result = DateParser.parse("31/02/2026")
        assertTrue(result is DateParseResult.NoCandidate)
    }

    @Test
    fun test10_validLeapYear() {
        val result = DateParser.parse("EXP 29/02/2024")
        assertTrue(result is DateParseResult.Candidates)
        val cand = (result as DateParseResult.Candidates).items[0]
        assertEquals(LocalDate.of(2024, 2, 29), cand.date)
    }

    @Test
    fun test11_invalidLeapYear() {
        val result = DateParser.parse("EXP 29/02/2025")
        assertTrue(result is DateParseResult.NoCandidate)
    }

    @Test
    fun test12_twoDigitYear() {
        val result = DateParser.parse("EXP 25/12/26")
        assertTrue(result is DateParseResult.Candidates)
        val cand = (result as DateParseResult.Candidates).items[0]
        assertEquals(LocalDate.of(2026, 12, 25), cand.date)
        assertEquals("dd/MM/yy", cand.matchedFormat)
        assertTrue(cand.warnings.any { it.contains("2026") })
    }

    @Test
    fun test13_monthYearOnly() {
        val result = DateParser.parse("12/2026")
        assertTrue(result is DateParseResult.NoCandidate)
    }

    @Test
    fun test14_textWithoutDigits() {
        val result = DateParser.parse("Kue Kering Cokelat Enak")
        assertTrue(result is DateParseResult.NoCandidate)
    }

    @Test
    fun test15_ocrTypoCorrection() {
        val result = DateParser.parse("EXP 2S/12/2026")
        assertTrue(result is DateParseResult.Candidates)
        val cand = (result as DateParseResult.Candidates).items[0]
        assertEquals(LocalDate.of(2026, 12, 25), cand.date)
        assertEquals("2S/12/2026", cand.rawText)
        assertEquals(CandidateConfidence.LOW, cand.confidence)
        assertTrue(cand.warnings.any { it.contains("Koreksi OCR") })
    }

    @Test
    fun testExtra_dotsAndSpacesInMonth() {
        val result = DateParser.parse("EXP: 15.DES.2025")
        assertTrue(result is DateParseResult.Candidates)
        val cand = (result as DateParseResult.Candidates).items[0]
        assertEquals(LocalDate.of(2025, 12, 15), cand.date)
        assertEquals(DateContext.EXPIRY, cand.context)
    }
}
