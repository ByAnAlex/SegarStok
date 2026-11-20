package com.segarstok.domain.usecase

import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.repository.StockRepository

class AddBatchUseCase(
    private val repository: StockRepository
) {
    suspend operator fun invoke(batch: StockBatch): Result<Long> {
        return try {
            if (batch.productName.trim().isEmpty()) {
                return Result.failure(IllegalArgumentException("Nama produk tidak boleh kosong"))
            }
            if (batch.quantity <= 0) {
                return Result.failure(IllegalArgumentException("Jumlah stok harus lebih besar dari 0"))
            }
            val newId = repository.insertBatch(batch)
            Result.success(newId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
