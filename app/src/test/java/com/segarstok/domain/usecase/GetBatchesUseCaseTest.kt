package com.segarstok.domain.usecase

import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.ProductCategory
import com.segarstok.domain.model.SortOption
import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.model.UrgencyLevel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class GetBatchesUseCaseTest {

    private lateinit var fakeRepository: FakeStockRepository
    private lateinit var getBatchesUseCase: GetBatchesUseCase

    private val batchToday = StockBatch(
        id = 1L,
        productName = "Nugget Ayam",
        category = ProductCategory.FROZEN_FOOD,
        quantity = 10.0,
        expiryDate = LocalDate.now(),
        status = BatchStatus.AVAILABLE
    )

    private val batchIn5Days = StockBatch(
        id = 2L,
        productName = "Sosis Sapi",
        category = ProductCategory.FROZEN_FOOD,
        quantity = 20.0,
        expiryDate = LocalDate.now().plusDays(5),
        status = BatchStatus.AVAILABLE
    )

    private val batchExpired = StockBatch(
        id = 3L,
        productName = "Roti Tawar",
        category = ProductCategory.BAKERY,
        quantity = 5.0,
        expiryDate = LocalDate.now().minusDays(2),
        status = BatchStatus.DISCARDED
    )

    @Before
    fun setUp() {
        fakeRepository = FakeStockRepository()
        getBatchesUseCase = GetBatchesUseCase(fakeRepository)

        runTest {
            fakeRepository.insertBatch(batchToday)
            fakeRepository.insertBatch(batchIn5Days)
            fakeRepository.insertBatch(batchExpired)
        }
    }

    @Test
    fun invoke_defaultSort_sortsByExpiryDateAscending() = runTest {
        val result = getBatchesUseCase().first()
        assertEquals(3, result.size)
        assertEquals(3L, result[0].id) // expired (minusDays(2))
        assertEquals(1L, result[1].id) // today
        assertEquals(2L, result[2].id) // plusDays(5)
    }

    @Test
    fun invoke_sortByExpiryDesc_sortsByExpiryDateDescending() = runTest {
        val result = getBatchesUseCase(sortBy = SortOption.EXPIRY_DESC).first()
        assertEquals(2L, result[0].id)
        assertEquals(1L, result[1].id)
        assertEquals(3L, result[2].id)
    }

    @Test
    fun invoke_searchQuery_filtersByName() = runTest {
        val result = getBatchesUseCase(searchQuery = "Nugget").first()
        assertEquals(1, result.size)
        assertEquals("Nugget Ayam", result[0].productName)
    }

    @Test
    fun invoke_categoryFilter_filtersByCategory() = runTest {
        val result = getBatchesUseCase(categoryFilter = ProductCategory.BAKERY.label).first()
        assertEquals(1, result.size)
        assertEquals("Roti Tawar", result[0].productName)
    }

    @Test
    fun invoke_statusFilter_filtersByStatus() = runTest {
        val result = getBatchesUseCase(statusFilter = BatchStatus.DISCARDED.name).first()
        assertEquals(1, result.size)
        assertEquals(BatchStatus.DISCARDED, result[0].status)
    }

    @Test
    fun invoke_urgencyFilter_filtersByUrgencyLevel() = runTest {
        val result = getBatchesUseCase(urgencyFilter = UrgencyLevel.DUE_TODAY.name).first()
        assertEquals(1, result.size)
        assertEquals("Nugget Ayam", result[0].productName)
    }
}
