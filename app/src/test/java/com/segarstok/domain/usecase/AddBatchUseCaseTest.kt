package com.segarstok.domain.usecase

import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.ProductCategory
import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class AddBatchUseCaseTest {

    private lateinit var fakeRepository: FakeStockRepository
    private lateinit var addBatchUseCase: AddBatchUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeStockRepository()
        addBatchUseCase = AddBatchUseCase(fakeRepository)
    }

    @Test
    fun addValidBatch_returnsSuccessWithId() = runBlocking {
        val validBatch = StockBatch(
            productName = "Nugget Ayam 500g",
            category = ProductCategory.FROZEN_FOOD,
            quantity = 10.0,
            unit = "pack",
            expiryDate = LocalDate.now().plusDays(10)
        )

        val result = addBatchUseCase(validBatch)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        assertEquals(1, fakeRepository.savedBatches.size)
    }

    @Test
    fun addBatchWithEmptyName_returnsFailure() = runBlocking {
        val invalidBatch = StockBatch(
            productName = "   ",
            category = ProductCategory.BAKERY,
            quantity = 5.0,
            unit = "pcs",
            expiryDate = LocalDate.now().plusDays(2)
        )

        val result = addBatchUseCase(invalidBatch)

        assertTrue(result.isFailure)
        assertEquals("Nama produk tidak boleh kosong", result.exceptionOrNull()?.message)
        assertEquals(0, fakeRepository.savedBatches.size)
    }

    @Test
    fun addBatchWithZeroQuantity_returnsFailure() = runBlocking {
        val invalidBatch = StockBatch(
            productName = "Sosis Sapi",
            category = ProductCategory.FROZEN_FOOD,
            quantity = 0.0,
            unit = "pack",
            expiryDate = LocalDate.now().plusDays(5)
        )

        val result = addBatchUseCase(invalidBatch)

        assertTrue(result.isFailure)
        assertEquals("Jumlah stok harus lebih besar dari 0", result.exceptionOrNull()?.message)
        assertEquals(0, fakeRepository.savedBatches.size)
    }
}

class FakeStockRepository : StockRepository {
    val savedBatches = mutableListOf<StockBatch>()
    private var currentId = 1L

    override fun getAllBatches(): Flow<List<StockBatch>> = flowOf(savedBatches)
    override fun getAvailableBatches(): Flow<List<StockBatch>> = flowOf(savedBatches.filter { it.status == BatchStatus.AVAILABLE })
    override fun getBatchById(id: Long): Flow<StockBatch?> = flowOf(savedBatches.find { it.id == id })
    override suspend fun getBatchByIdDirect(id: Long): StockBatch? = savedBatches.find { it.id == id }

    override suspend fun insertBatch(batch: StockBatch): Long {
        val newBatch = batch.copy(id = currentId++)
        savedBatches.add(newBatch)
        return newBatch.id
    }

    override suspend fun updateBatch(batch: StockBatch) {
        val index = savedBatches.indexOfFirst { it.id == batch.id }
        if (index != -1) {
            savedBatches[index] = batch
        }
    }

    override suspend fun updateBatchStatus(id: Long, newStatus: BatchStatus, newQuantity: Double?) {
        val index = savedBatches.indexOfFirst { it.id == id }
        if (index != -1) {
            val existing = savedBatches[index]
            savedBatches[index] = existing.copy(
                status = newStatus,
                quantity = newQuantity ?: existing.quantity
            )
        }
    }

    override suspend fun deleteBatch(id: Long) {
        savedBatches.removeAll { it.id == id }
    }
}
