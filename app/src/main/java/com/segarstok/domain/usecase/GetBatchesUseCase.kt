package com.segarstok.domain.usecase

import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetBatchesUseCase(
    private val repository: StockRepository
) {
    operator fun invoke(
        searchQuery: String = "",
        categoryFilter: String = "Semua",
        statusFilter: BatchStatus? = null
    ): Flow<List<StockBatch>> {
        return repository.getAllBatches().map { list ->
            list.filter { batch ->
                val matchesSearch = searchQuery.isBlank() || batch.productName.contains(searchQuery, ignoreCase = true)
                val matchesCategory = categoryFilter == "Semua" || batch.category.label.equals(categoryFilter, ignoreCase = true)
                val matchesStatus = statusFilter == null || batch.status == statusFilter
                matchesSearch && matchesCategory && matchesStatus
            }.sortedBy { it.expiryDate }
        }
    }
}
