package com.segarstok.domain.usecase

import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.repository.StockRepository

class ReduceStockQuantityUseCase(
    private val repository: StockRepository
) {
    suspend operator fun invoke(batchId: Long, reduceAmount: Double): Result<Unit> {
        return try {
            if (reduceAmount <= 0) {
                return Result.failure(IllegalArgumentException("Jumlah pemakaian harus lebih besar dari 0"))
            }

            val existing = repository.getBatchByIdDirect(batchId)
                ?: return Result.failure(IllegalArgumentException("Batch dengan ID $batchId tidak ditemukan"))

            if (reduceAmount > existing.quantity) {
                return Result.failure(
                    IllegalArgumentException(
                        "Jumlah pemakaian ($reduceAmount ${existing.unit}) melebihi sisa stok yang ada (${existing.quantity} ${existing.unit})"
                    )
                )
            }

            val remainingQuantity = existing.quantity - reduceAmount
            val newStatus = if (remainingQuantity == 0.0) {
                BatchStatus.SOLD_OR_USED
            } else {
                existing.status
            }

            repository.updateBatchStatus(
                id = batchId,
                newStatus = newStatus,
                newQuantity = remainingQuantity
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
