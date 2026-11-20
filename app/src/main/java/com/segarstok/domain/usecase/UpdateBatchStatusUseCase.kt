package com.segarstok.domain.usecase

import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.repository.StockRepository

class UpdateBatchStatusUseCase(
    private val repository: StockRepository
) {
    suspend operator fun invoke(id: Long, newStatus: BatchStatus, newQuantity: Double? = null): Result<Unit> {
        return try {
            if (newQuantity != null && newQuantity < 0) {
                return Result.failure(IllegalArgumentException("Jumlah stok tidak boleh negatif"))
            }
            repository.updateBatchStatus(id, newStatus, newQuantity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
