package com.segarstok.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ExpiryLossMonitorTest {

    @Test
    fun remainingRiskValue_integerQuantity_calculatesCorrectValue() {
        val batch = StockBatch(
            productName = "Nugget Ayam",
            quantity = 10.0,
            expiryDate = LocalDate.now().plusDays(5),
            purchasePricePerUnit = 10000L,
            status = BatchStatus.AVAILABLE
        )

        assertEquals(100000L, batch.remainingRiskValue())
    }

    @Test
    fun remainingRiskValue_decimalQuantity_calculatesCorrectValue() {
        val batch = StockBatch(
            productName = "Daging Sapi (kg)",
            quantity = 2.5,
            expiryDate = LocalDate.now().plusDays(5),
            purchasePricePerUnit = 120000L,
            status = BatchStatus.AVAILABLE
        )

        assertEquals(300000L, batch.remainingRiskValue())
    }

    @Test
    fun remainingRiskValue_nullPurchasePrice_returnsNull() {
        val batch = StockBatch(
            productName = "Kue Rumahan",
            quantity = 5.0,
            expiryDate = LocalDate.now().plusDays(2),
            purchasePricePerUnit = null,
            status = BatchStatus.AVAILABLE
        )

        assertNull(batch.remainingRiskValue())
    }

    @Test
    fun remainingRiskValue_nonAvailableStatus_returnsNull() {
        val soldBatch = StockBatch(
            productName = "Sosis Sapi",
            quantity = 10.0,
            expiryDate = LocalDate.now().plusDays(5),
            purchasePricePerUnit = 15000L,
            status = BatchStatus.SOLD_OR_USED
        )

        val discardedBatch = StockBatch(
            productName = "Sosis Sapi",
            quantity = 10.0,
            expiryDate = LocalDate.now().minusDays(1),
            purchasePricePerUnit = 15000L,
            status = BatchStatus.DISCARDED
        )

        assertNull(soldBatch.remainingRiskValue())
        assertNull(discardedBatch.remainingRiskValue())
    }
}
