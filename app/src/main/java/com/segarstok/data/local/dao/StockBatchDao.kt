package com.segarstok.data.local.dao

import androidx.room.*
import com.segarstok.data.local.entity.StockBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockBatchDao {

    @Query("SELECT * FROM stock_batches ORDER BY expiryEpochDay ASC")
    fun getAllBatches(): Flow<List<StockBatchEntity>>

    @Query("SELECT * FROM stock_batches WHERE status = :status ORDER BY expiryEpochDay ASC")
    fun getBatchesByStatus(status: String): Flow<List<StockBatchEntity>>

    @Query("SELECT * FROM stock_batches WHERE id = :id")
    fun getBatchById(id: Long): Flow<StockBatchEntity?>

    @Query("SELECT * FROM stock_batches WHERE id = :id")
    suspend fun getBatchByIdDirect(id: Long): StockBatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: StockBatchEntity): Long

    @Update
    suspend fun updateBatch(batch: StockBatchEntity)

    @Delete
    suspend fun deleteBatch(batch: StockBatchEntity)

    @Query("DELETE FROM stock_batches WHERE id = :id")
    suspend fun deleteBatchById(id: Long)
}
