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
class ReduceStockQuantityUseCaseTest {

    private lateinit var fakeRepository: FakeStockRepository
    private lateinit var reduceStockQuantityUseCase: ReduceStockQuantityUseCase
    private var insertedId: Long = 0L

    private val sampleBatch = StockBatch(
        productName = "Nugget Ayam 500g",
        category = ProductCategory.FROZEN_FOOD,
        quantity = 10.0,
        unit = "pack",
        expiryDate = LocalDate.now().plusDays(10),
        status = BatchStatus.AVAILABLE
    )

    @Before
    fun setUp() {
        fakeRepository = FakeStockRepository()
        reduceStockQuantityUseCase = ReduceStockQuantityUseCase(fakeRepository)

        runTest {
            insertedId = fakeRepository.insertBatch(sampleBatch)
        }
    }

    @Test
    fun reduceQuantity_partialConsumption_updatesQuantityAndKeepsAvailableStatus() = runTest {
        val result = reduceStockQuantityUseCase(insertedId, 3.0)

        assertTrue(result.isSuccess)
        val fetched = fakeRepository.getBatchByIdDirect(insertedId)
        assertNotNull(fetched)
        assertEquals(7.0, fetched!!.quantity, 0.001)
        assertEquals(BatchStatus.AVAILABLE, fetched.status)
    }

    @Test
    fun reduceQuantity_fullConsumption_updatesQuantityToZeroAndAutoSwitchesToSoldOrUsed() = runTest {
        val result = reduceStockQuantityUseCase(insertedId, 10.0)

        assertTrue(result.isSuccess)
        val fetched = fakeRepository.getBatchByIdDirect(insertedId)
        assertNotNull(fetched)
        assertEquals(0.0, fetched!!.quantity, 0.001)
        assertEquals(BatchStatus.SOLD_OR_USED, fetched.status)
    }

    @Test
    fun reduceQuantity_zeroOrNegativeAmount_returnsFailure() = runTest {
        val resultZero = reduceStockQuantityUseCase(insertedId, 0.0)
        assertTrue(resultZero.isFailure)
        assertEquals("Jumlah pemakaian harus lebih besar dari 0", resultZero.exceptionOrNull()?.message)

        val resultNeg = reduceStockQuantityUseCase(insertedId, -2.0)
        assertTrue(resultNeg.isFailure)
        assertEquals("Jumlah pemakaian harus lebih besar dari 0", resultNeg.exceptionOrNull()?.message)
    }

    @Test
    fun reduceQuantity_amountExceedingCurrentQuantity_returnsFailure() = runTest {
        val resultExceed = reduceStockQuantityUseCase(insertedId, 15.0)

        assertTrue(resultExceed.isFailure)
        assertTrue(resultExceed.exceptionOrNull()?.message?.contains("melebihi sisa stok") == true)
    }
}
