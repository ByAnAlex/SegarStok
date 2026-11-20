package com.segarstok.data.repository

import com.segarstok.data.local.dao.StockBatchDao
import com.segarstok.data.local.toDomain
import com.segarstok.data.local.toEntity
import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StockRepositoryImpl(
    private val dao: StockBatchDao
) : StockRepository {

    override fun getAllBatches(): Flow<List<StockBatch>> {
        return dao.getAllBatches().map { list -> list.map { it.toDomain() } }
    }

    override fun getAvailableBatches(): Flow<List<StockBatch>> {
        return dao.getBatchesByStatus(BatchStatus.AVAILABLE.name).map { list -> list.map { it.toDomain() } }
    }

    override fun getBatchById(id: Long): Flow<StockBatch?> {
        return dao.getBatchById(id).map { it?.toDomain() }
    }

    override suspend fun getBatchByIdDirect(id: Long): StockBatch? {
        return dao.getBatchByIdDirect(id)?.toDomain()
    }

    override suspend fun insertBatch(batch: StockBatch): Long {
        return dao.insertBatch(batch.toEntity())
    }

    override suspend fun updateBatch(batch: StockBatch) {
        dao.updateBatch(batch.toEntity())
    }

    override suspend fun updateBatchStatus(id: Long, newStatus: BatchStatus, newQuantity: Double?) {
        val existing = dao.getBatchByIdDirect(id) ?: return
        val updated = existing.copy(
            status = newStatus.name,
            quantity = newQuantity ?: existing.quantity,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateBatch(updated)
    }

    override suspend fun deleteBatch(id: Long) {
        dao.deleteBatchById(id)
    }
}
