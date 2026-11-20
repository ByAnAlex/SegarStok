package com.segarstok.domain.usecase

import com.segarstok.domain.repository.StockRepository

class DeleteBatchUseCase(
    private val repository: StockRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return try {
            repository.deleteBatch(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
