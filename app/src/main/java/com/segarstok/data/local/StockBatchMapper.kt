package com.segarstok.data.local

import com.segarstok.data.local.entity.StockBatchEntity
import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.ProductCategory
import com.segarstok.domain.model.StockBatch
import java.time.LocalDate

fun StockBatchEntity.toDomain(): StockBatch {
    val categoryEnum = try {
        ProductCategory.valueOf(category)
    } catch (e: Exception) {
        ProductCategory.OTHER
    }

    val statusEnum = try {
        BatchStatus.valueOf(status)
    } catch (e: Exception) {
        BatchStatus.AVAILABLE
    }

    return StockBatch(
        id = id,
        productName = productName,
        category = categoryEnum,
        quantity = quantity,
        unit = unit,
        expiryDate = LocalDate.ofEpochDay(expiryEpochDay),
        purchasePricePerUnit = purchasePricePerUnit,
        normalSellingPricePerUnit = normalSellingPricePerUnit,
        status = statusEnum,
        dateEntryMethod = dateEntryMethod,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun StockBatch.toEntity(): StockBatchEntity {
    return StockBatchEntity(
        id = id,
        productName = productName,
        category = category.name,
        quantity = quantity,
        unit = unit,
        expiryEpochDay = expiryDate.toEpochDay(),
        purchasePricePerUnit = purchasePricePerUnit,
        normalSellingPricePerUnit = normalSellingPricePerUnit,
        status = status.name,
        dateEntryMethod = dateEntryMethod,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
