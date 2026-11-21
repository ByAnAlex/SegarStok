package com.segarstok.domain.usecase

import com.segarstok.domain.recommendation.BatchRecommendation
import com.segarstok.domain.recommendation.RecommendationEngine
import com.segarstok.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class GetRecommendationsUseCase(
    private val repository: StockRepository
) {
    operator fun invoke(
        discountRate: Double = 0.10,
        currentDate: LocalDate = LocalDate.now()
    ): Flow<List<BatchRecommendation>> {
        return repository.getAvailableBatches().map { availableBatches ->
            availableBatches.mapNotNull { batch ->
                RecommendationEngine.generateRecommendation(
                    batch = batch,
                    discountRate = discountRate,
                    currentDate = currentDate
                )
            }.sortedBy { it.daysLeft }
        }
    }
}
