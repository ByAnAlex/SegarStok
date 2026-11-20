package com.segarstok.data.local

import com.segarstok.data.local.entity.StockBatchEntity
import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.ProductCategory
import com.segarstok.domain.model.StockBatch
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StockBatchMapperTest {

    @Test
    fun entityToDomain_mapsFieldsCorrectly() {
        val entity = StockBatchEntity(
            id = 10L,
            productName = "Susu UHT 1L",
            category = "PACKAGED_BEVERAGE",
            quantity = 15.0,
            unit = "karton",
            expiryEpochDay = LocalDate.of(2026, 12, 31).toEpochDay(),
            purchasePricePerUnit = 12000L,
            normalSellingPricePerUnit = 16000L,
            status = "AVAILABLE",
            dateEntryMethod = "OCR_CONFIRMED",
            notes = "Simpan dingin"
        )

        val domain = entity.toDomain()

        assertEquals(10L, domain.id)
        assertEquals("Susu UHT 1L", domain.productName)
        assertEquals(ProductCategory.PACKAGED_BEVERAGE, domain.category)
        assertEquals(15.0, domain.quantity, 0.001)
        assertEquals("karton", domain.unit)
        assertEquals(LocalDate.of(2026, 12, 31), domain.expiryDate)
        assertEquals(12000L, domain.purchasePricePerUnit)
        assertEquals(16000L, domain.normalSellingPricePerUnit)
        assertEquals(BatchStatus.AVAILABLE, domain.status)
        assertEquals("OCR_CONFIRMED", domain.dateEntryMethod)
        assertEquals("Simpan dingin", domain.notes)
    }

    @Test
    fun domainToEntity_mapsFieldsCorrectly() {
        val domain = StockBatch(
            id = 5L,
            productName = "Roti Tawar",
            category = ProductCategory.BAKERY,
            quantity = 8.0,
            unit = "bungkus",
            expiryDate = LocalDate.of(2026, 11, 25),
            purchasePricePerUnit = 8000L,
            normalSellingPricePerUnit = 12000L,
            status = BatchStatus.SOLD_OR_USED,
            dateEntryMethod = "MANUAL",
            notes = "Segera dijual"
        )

        val entity = domain.toEntity()

        assertEquals(5L, entity.id)
        assertEquals("Roti Tawar", entity.productName)
        assertEquals("BAKERY", entity.category)
        assertEquals(8.0, entity.quantity, 0.001)
        assertEquals("bungkus", entity.unit)
        assertEquals(LocalDate.of(2026, 11, 25).toEpochDay(), entity.expiryEpochDay)
        assertEquals(8000L, entity.purchasePricePerUnit)
        assertEquals(12000L, entity.normalSellingPricePerUnit)
        assertEquals("SOLD_OR_USED", entity.status)
        assertEquals("MANUAL", entity.dateEntryMethod)
        assertEquals("Segera dijual", entity.notes)
    }
}
