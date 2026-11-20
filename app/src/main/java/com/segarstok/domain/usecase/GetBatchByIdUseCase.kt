package com.segarstok.domain.usecase

import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow

class GetBatchByIdUseCase(
    private val repository: StockRepository
) {
    operator fun invoke(id: Long): Flow<StockBatch?> {
        return repository.getBatchById(id)
    }

    suspend fun getDirect(id: Long): StockBatch? {
        return repository.getBatchByIdDirect(id)
    }
}
