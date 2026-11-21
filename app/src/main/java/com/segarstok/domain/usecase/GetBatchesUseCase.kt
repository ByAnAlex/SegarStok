package com.segarstok.domain.usecase

import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.SortOption
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
        statusFilter: String = "Semua",
        urgencyFilter: String = "Semua",
        sortBy: SortOption = SortOption.EXPIRY_ASC
    ): Flow<List<StockBatch>> {
        return repository.getAllBatches().map { list ->
            list.filter { batch ->
                val matchesSearch = searchQuery.isBlank() || batch.productName.contains(searchQuery, ignoreCase = true)

                val matchesCategory = categoryFilter == "Semua" ||
                        batch.category.label.equals(categoryFilter, ignoreCase = true) ||
                        batch.category.name.equals(categoryFilter, ignoreCase = true)

                val matchesStatus = statusFilter == "Semua" ||
                        batch.status.name.equals(statusFilter, ignoreCase = true) ||
                        batch.status.label.equals(statusFilter, ignoreCase = true)

                val matchesUrgency = urgencyFilter == "Semua" ||
                        batch.urgencyLevel().name.equals(urgencyFilter, ignoreCase = true) ||
                        batch.urgencyLevel().label.equals(urgencyFilter, ignoreCase = true)

                matchesSearch && matchesCategory && matchesStatus && matchesUrgency
            }.let { filtered ->
                when (sortBy) {
                    SortOption.EXPIRY_ASC -> filtered.sortedBy { it.expiryDate }
                    SortOption.EXPIRY_DESC -> filtered.sortedByDescending { it.expiryDate }
                    SortOption.NAME_ASC -> filtered.sortedBy { it.productName.lowercase() }
                    SortOption.QUANTITY_DESC -> filtered.sortedByDescending { it.quantity }
                }
            }
        }
    }
}
