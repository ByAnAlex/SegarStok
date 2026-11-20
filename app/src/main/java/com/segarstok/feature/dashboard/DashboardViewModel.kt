package com.segarstok.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.repository.StockRepository
import com.segarstok.domain.usecase.GetBatchesUseCase
import kotlinx.coroutines.flow.*

data class DashboardUiState(
    val totalAvailableCount: Int = 0,
    val urgentCount: Int = 0,
    val expiredCount: Int = 0,
    val totalRiskValue: Long = 0L,
    val priorityBatches: List<StockBatch> = emptyList(),
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val getBatchesUseCase: GetBatchesUseCase,
    private val repository: StockRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = getBatchesUseCase().map { allBatches ->
        val availableList = allBatches.filter { it.status == BatchStatus.AVAILABLE }
        val urgentList = availableList.filter { it.daysLeft() in 0..3 }
        val expiredList = availableList.filter { it.daysLeft() < 0 }

        val totalRisk = availableList.sumOf { it.remainingRiskValue() ?: 0L }

        DashboardUiState(
            totalAvailableCount = availableList.size,
            urgentCount = urgentList.size,
            expiredCount = expiredList.size,
            totalRiskValue = totalRisk,
            priorityBatches = availableList.take(5),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )
}
