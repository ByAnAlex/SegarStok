package com.segarstok.feature.batchdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.usecase.DeleteBatchUseCase
import com.segarstok.domain.usecase.GetBatchByIdUseCase
import com.segarstok.domain.usecase.UpdateBatchStatusUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BatchDetailUiState(
    val batch: StockBatch? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isActionSuccess: Boolean = false
)

class BatchDetailViewModel(
    private val batchId: Long,
    private val getBatchByIdUseCase: GetBatchByIdUseCase,
    private val updateBatchStatusUseCase: UpdateBatchStatusUseCase,
    private val deleteBatchUseCase: DeleteBatchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchDetailUiState(isLoading = true))
    val uiState: StateFlow<BatchDetailUiState> = _uiState.asStateFlow()

    init {
        observeBatch()
    }

    private fun observeBatch() {
        viewModelScope.launch {
            getBatchByIdUseCase(batchId).collect { batch ->
                _uiState.value = _uiState.value.copy(
                    batch = batch,
                    isLoading = false
                )
            }
        }
    }

    fun updateStatus(newStatus: BatchStatus, newQuantity: Double? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = updateBatchStatusUseCase(batchId, newStatus, newQuantity)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, isActionSuccess = true)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = err.message ?: "Gagal memperbarui status batch")
            }
        }
    }

    fun deleteBatch(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = deleteBatchUseCase(batchId)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = err.message ?: "Gagal menghapus batch")
            }
        }
    }
}
