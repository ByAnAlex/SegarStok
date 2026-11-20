package com.segarstok.domain.repository

import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.StockBatch
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun getAllBatches(): Flow<List<StockBatch>>
    fun getAvailableBatches(): Flow<List<StockBatch>>
    fun getBatchById(id: Long): Flow<StockBatch?>
    suspend fun getBatchByIdDirect(id: Long): StockBatch?
    suspend fun insertBatch(batch: StockBatch): Long
    suspend fun updateBatch(batch: StockBatch)
    suspend fun updateBatchStatus(id: Long, newStatus: BatchStatus, newQuantity: Double? = null)
    suspend fun deleteBatch(id: Long)
}
