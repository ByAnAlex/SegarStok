package com.segarstok.domain.recommendation

import org.junit.Assert.*
import org.junit.Test

class PromoCalculatorTest {

    @Test
    fun calculate_withValidPricesAnd10PercentDiscount_returnsCorrectPromoPriceAndPositiveMargin() {
        val result = PromoCalculator.calculate(
            normalSellingPricePerUnit = 15000L,
            purchasePricePerUnit = 10000L,
            discountRate = 0.10
        )

        assertEquals(0.10, result.discountRate, 0.001)
        assertEquals(13500L, result.promoPrice)
        assertEquals(3500L, result.grossMarginPerUnit)
        assertNull(result.marginWarning)
        assertTrue(result.formattedText.contains("Diskon 10%"))
        assertTrue(result.formattedText.contains("Rp 13.500"))
        assertTrue(result.formattedText.contains("Rp 3.500/unit"))
    }

    @Test
    fun calculate_withHighDiscount_returnsNegativeMarginWarning() {
        val result = PromoCalculator.calculate(
            normalSellingPricePerUnit = 15000L,
            purchasePricePerUnit = 12000L,
            discountRate = 0.30
        )

        assertEquals(10500L, result.promoPrice)
        assertEquals(-1500L, result.grossMarginPerUnit)
        assertNotNull(result.marginWarning)
        assertTrue(result.marginWarning!!.contains("Margin kotor nol atau negatif"))
    }

    @Test
    fun calculate_whenNormalSellingPriceIsNull_returnsMissingPriceWarning() {
        val result = PromoCalculator.calculate(
            normalSellingPricePerUnit = null,
            purchasePricePerUnit = 10000L,
            discountRate = 0.10
        )

        assertNull(result.promoPrice)
        assertNull(result.grossMarginPerUnit)
        assertNotNull(result.marginWarning)
        assertTrue(result.marginWarning!!.contains("Harga jual normal belum diisi"))
    }

    @Test
    fun calculate_whenPurchasePriceIsNull_returnsPromoPriceWithoutMargin() {
        val result = PromoCalculator.calculate(
            normalSellingPricePerUnit = 20000L,
            purchasePricePerUnit = null,
            discountRate = 0.20
        )

        assertEquals(16000L, result.promoPrice)
        assertNull(result.grossMarginPerUnit)
        assertNotNull(result.marginWarning)
        assertTrue(result.marginWarning!!.contains("Harga beli belum diisi"))
    }
}
