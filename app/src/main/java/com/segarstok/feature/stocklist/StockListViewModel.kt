package com.segarstok.feature.stocklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segarstok.domain.model.SortOption
import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.usecase.GetBatchesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class StockListUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "Semua",
    val selectedStatus: String = "Semua",
    val selectedUrgency: String = "Semua",
    val selectedSortOption: SortOption = SortOption.EXPIRY_ASC,
    val batches: List<StockBatch> = emptyList(),
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class StockListViewModel(
    private val getBatchesUseCase: GetBatchesUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("Semua")
    private val _selectedStatus = MutableStateFlow("Semua")
    private val _selectedUrgency = MutableStateFlow("Semua")
    private val _selectedSortOption = MutableStateFlow(SortOption.EXPIRY_ASC)

    val uiState: StateFlow<StockListUiState> = combine(
        _searchQuery,
        _selectedCategory,
        _selectedStatus,
        _selectedUrgency,
        _selectedSortOption
    ) { query, category, status, urgency, sort ->
        FilterState(query, category, status, urgency, sort)
    }.flatMapLatest { filter ->
        getBatchesUseCase(
            searchQuery = filter.query,
            categoryFilter = filter.category,
            statusFilter = filter.status,
            urgencyFilter = filter.urgency,
            sortBy = filter.sortOption
        ).map { batchList ->
            StockListUiState(
                searchQuery = filter.query,
                selectedCategory = filter.category,
                selectedStatus = filter.status,
                selectedUrgency = filter.urgency,
                selectedSortOption = filter.sortOption,
                batches = batchList,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StockListUiState(isLoading = true)
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategoryChange(newCategory: String) {
        _selectedCategory.value = newCategory
    }

    fun onStatusChange(newStatus: String) {
        _selectedStatus.value = newStatus
    }

    fun onUrgencyChange(newUrgency: String) {
        _selectedUrgency.value = newUrgency
    }

    fun onSortOptionChange(newSort: SortOption) {
        _selectedSortOption.value = newSort
    }
}

private data class FilterState(
    val query: String,
    val category: String,
    val status: String,
    val urgency: String,
    val sortOption: SortOption
)
