package com.segarstok.domain.recommendation

import com.segarstok.domain.model.ProductCategory
import com.segarstok.domain.model.UrgencyLevel

data class PromoSimulationResult(
    val discountRate: Double,
    val promoPrice: Long?,
    val grossMarginPerUnit: Long?,
    val marginWarning: String?,
    val formattedText: String
)

data class BatchRecommendation(
    val batchId: Long,
    val productName: String,
    val category: ProductCategory,
    val quantity: Double,
    val unit: String,
    val daysLeft: Long,
    val urgencyLevel: UrgencyLevel,
    val actionDirection: String,
    val detailedAdvice: String,
    val categoryAdvice: String,
    val promoSimulation: PromoSimulationResult?,
    val isExpiredGuardrail: Boolean
)
