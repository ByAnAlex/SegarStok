package com.segarstok.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_batches")
data class StockBatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val category: String = "OTHER",
    val quantity: Double,
    val unit: String = "pack",
    val expiryEpochDay: Long,
    val purchasePricePerUnit: Long? = null,
    val normalSellingPricePerUnit: Long? = null,
    val status: String = "AVAILABLE",
    val dateEntryMethod: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
