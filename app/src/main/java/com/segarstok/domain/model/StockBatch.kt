package com.segarstok.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class StockBatch(
    val id: Long = 0,
    val productName: String,
    val category: ProductCategory = ProductCategory.OTHER,
    val quantity: Double,
    val unit: String = "pack",
    val expiryDate: LocalDate,
    val purchasePricePerUnit: Long? = null,
    val normalSellingPricePerUnit: Long? = null,
    val status: BatchStatus = BatchStatus.AVAILABLE,
    val dateEntryMethod: String? = "MANUAL",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun daysLeft(currentDate: LocalDate = LocalDate.now()): Long {
        return ChronoUnit.DAYS.between(currentDate, expiryDate)
    }

    fun urgencyLevel(currentDate: LocalDate = LocalDate.now()): UrgencyLevel {
        return UrgencyLevel.fromDaysLeft(daysLeft(currentDate))
    }

    fun remainingRiskValue(): Long? {
        if (status != BatchStatus.AVAILABLE || purchasePricePerUnit == null) return null
        return (quantity * purchasePricePerUnit).toLong()
    }
}
