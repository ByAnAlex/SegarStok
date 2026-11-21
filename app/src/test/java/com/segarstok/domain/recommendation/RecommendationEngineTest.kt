package com.segarstok.domain.recommendation

import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.ProductCategory
import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.model.UrgencyLevel
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class RecommendationEngineTest {

    private val today = LocalDate.of(2026, 11, 20)

    @Test
    fun generateRecommendation_whenBatchStatusNotAvailable_returnsNull() {
        val batch = StockBatch(
            id = 1,
            productName = "Sosis Sapi",
            quantity = 5.0,
            expiryDate = today.plusDays(5),
            status = BatchStatus.SOLD_OR_USED
        )

        val result = RecommendationEngine.generateRecommendation(batch, currentDate = today)
        assertNull(result)
    }

    @Test
    fun generateRecommendation_whenQuantityZero_returnsNull() {
        val batch = StockBatch(
            id = 2,
            productName = "Nugget Ayam",
            quantity = 0.0,
            expiryDate = today.plusDays(5),
            status = BatchStatus.AVAILABLE
        )

        val result = RecommendationEngine.generateRecommendation(batch, currentDate = today)
        assertNull(result)
    }

    @Test
    fun generateRecommendation_whenExpired_returnsFoodSafetyGuardrail() {
        val batch = StockBatch(
            id = 3,
            productName = "Susu UHT",
            category = ProductCategory.PACKAGED_BEVERAGE,
            quantity = 10.0,
            expiryDate = today.minusDays(2),
            status = BatchStatus.AVAILABLE
        )

        val result = RecommendationEngine.generateRecommendation(batch, currentDate = today)

        assertNotNull(result)
        assertEquals(UrgencyLevel.EXPIRED, result!!.urgencyLevel)
        assertTrue(result.isExpiredGuardrail)
        assertNull(result.promoSimulation)
        assertTrue(result.detailedAdvice.contains("Hentikan penjualan segera"))
        assertTrue(result.categoryAdvice.contains("Dilarang menjual, mendiskon, mengonsumsi"))
    }

    @Test
    fun generateRecommendation_whenDueToday_returnsDayZeroCheckAndPromo() {
        val batch = StockBatch(
            id = 4,
            productName = "Roti Cokelat",
            category = ProductCategory.BAKERY,
            quantity = 5.0,
            expiryDate = today,
            normalSellingPricePerUnit = 10000L,
            purchasePricePerUnit = 6000L,
            status = BatchStatus.AVAILABLE
        )

        val result = RecommendationEngine.generateRecommendation(batch, discountRate = 0.20, currentDate = today)

        assertNotNull(result)
        assertEquals(UrgencyLevel.DUE_TODAY, result!!.urgencyLevel)
        assertFalse(result.isExpiredGuardrail)
        assertNotNull(result.promoSimulation)
        assertEquals(8000L, result.promoSimulation!!.promoPrice)
        assertEquals(2000L, result.promoSimulation!!.grossMarginPerUnit)
        assertTrue(result.categoryAdvice.contains("Bakery"))
    }

    @Test
    fun generateRecommendation_whenUrgent1To3Days_returnsUrgentPromoAdvice() {
        val batch = StockBatch(
            id = 5,
            productName = "Nugget Ayam 500g",
            category = ProductCategory.FROZEN_FOOD,
            quantity = 12.0,
            expiryDate = today.plusDays(2),
            normalSellingPricePerUnit = 20000L,
            purchasePricePerUnit = 14000L,
            status = BatchStatus.AVAILABLE
        )

        val result = RecommendationEngine.generateRecommendation(batch, discountRate = 0.10, currentDate = today)

        assertNotNull(result)
        assertEquals(UrgencyLevel.URGENT, result!!.urgencyLevel)
        assertEquals(18000L, result.promoSimulation!!.promoPrice)
        assertEquals(4000L, result.promoSimulation!!.grossMarginPerUnit)
        assertTrue(result.categoryAdvice.contains("Dilarang mencairkan (thaw) lalu membekukan ulang (refreeze)"))
    }

    @Test
    fun generateRecommendation_whenNormalMoreThan14Days_returnsNormalRotationAdvice() {
        val batch = StockBatch(
            id = 6,
            productName = "Keripik Singkong",
            category = ProductCategory.PACKAGED_SNACK,
            quantity = 50.0,
            expiryDate = today.plusDays(30),
            status = BatchStatus.AVAILABLE
        )

        val result = RecommendationEngine.generateRecommendation(batch, currentDate = today)

        assertNotNull(result)
        assertEquals(UrgencyLevel.NORMAL, result!!.urgencyLevel)
        assertNull(result.promoSimulation)
        assertTrue(result.actionDirection.contains("Rotasi Normal"))
    }
}
