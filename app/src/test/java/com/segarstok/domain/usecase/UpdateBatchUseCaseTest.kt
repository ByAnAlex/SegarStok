package com.segarstok.domain.usecase

import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.ProductCategory
import com.segarstok.domain.model.StockBatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateBatchUseCaseTest {

    private lateinit var fakeRepository: FakeStockRepository
    private lateinit var updateBatchUseCase: UpdateBatchUseCase
    private var insertedId: Long = 0L

    private val sampleBatch = StockBatch(
        productName = "Susu UHT",
        category = ProductCategory.PACKAGED_BEVERAGE,
        quantity = 24.0,
        expiryDate = LocalDate.now().plusDays(30),
        status = BatchStatus.AVAILABLE
    )

    @Before
    fun setUp() {
        fakeRepository = FakeStockRepository()
        updateBatchUseCase = UpdateBatchUseCase(fakeRepository)

        runTest {
            insertedId = fakeRepository.insertBatch(sampleBatch)
        }
    }

    @Test
    fun updateBatch_validBatch_updatesSuccessfully() = runTest {
        val inserted = fakeRepository.getBatchByIdDirect(insertedId)!!
        val updated = inserted.copy(productName = "Susu UHT Cokelat", quantity = 12.0)
        val result = updateBatchUseCase(updated)

        assertTrue(result.isSuccess)
        val fetched = fakeRepository.getBatchByIdDirect(insertedId)
        assertNotNull(fetched)
        assertEquals("Susu UHT Cokelat", fetched!!.productName)
        assertEquals(12.0, fetched.quantity, 0.001)
    }

    @Test
    fun updateBatch_blankName_returnsFailure() = runTest {
        val inserted = fakeRepository.getBatchByIdDirect(insertedId)!!
        val invalid = inserted.copy(productName = "  ")
        val result = updateBatchUseCase(invalid)

        assertTrue(result.isFailure)
        assertEquals("Nama produk wajib diisi", result.exceptionOrNull()?.message)
    }

    @Test
    fun updateBatch_invalidQuantity_returnsFailure() = runTest {
        val inserted = fakeRepository.getBatchByIdDirect(insertedId)!!
        val invalid = inserted.copy(quantity = 0.0)
        val result = updateBatchUseCase(invalid)

        assertTrue(result.isFailure)
        assertEquals("Jumlah stok harus lebih besar dari 0", result.exceptionOrNull()?.message)
    }

    @Test
    fun updateBatch_nonExistentId_returnsFailure() = runTest {
        val inserted = fakeRepository.getBatchByIdDirect(insertedId)!!
        val nonExistent = inserted.copy(id = 999L)
        val result = updateBatchUseCase(nonExistent)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("tidak ditemukan") == true)
    }
}
