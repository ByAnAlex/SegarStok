package com.segarstok.domain.usecase

import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.repository.StockRepository

class UpdateBatchUseCase(
    private val repository: StockRepository
) {
    suspend operator fun invoke(batch: StockBatch): Result<Unit> {
        return try {
            if (batch.productName.isBlank()) {
                return Result.failure(IllegalArgumentException("Nama produk wajib diisi"))
            }
            if (batch.quantity <= 0) {
                return Result.failure(IllegalArgumentException("Jumlah stok harus lebih besar dari 0"))
            }
            val existing = repository.getBatchByIdDirect(batch.id)
                ?: return Result.failure(IllegalArgumentException("Batch dengan ID ${batch.id} tidak ditemukan"))

            val updated = batch.copy(
                updatedAt = System.currentTimeMillis()
            )
            repository.updateBatch(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
