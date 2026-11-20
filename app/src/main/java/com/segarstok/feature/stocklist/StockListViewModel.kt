package com.segarstok.feature.stocklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.usecase.GetBatchesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class StockListUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "Semua",
    val batches: List<StockBatch> = emptyList(),
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class StockListViewModel(
    private val getBatchesUseCase: GetBatchesUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("Semua")

    val uiState: StateFlow<StockListUiState> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        query to category
    }.flatMapLatest { (query, category) ->
        getBatchesUseCase(searchQuery = query, categoryFilter = category).map { batchList ->
            StockListUiState(
                searchQuery = query,
                selectedCategory = category,
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
}
