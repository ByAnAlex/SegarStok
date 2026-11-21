package com.segarstok.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segarstok.domain.model.StockBatch
import com.segarstok.domain.model.UrgencyLevel
import com.segarstok.domain.usecase.GetBatchesUseCase
import kotlinx.coroutines.flow.*

data class DashboardUiState(
    val totalAvailableCount: Int = 0,
    val normalCount: Int = 0,
    val attentionCount: Int = 0,
    val nearCount: Int = 0,
    val urgentCount: Int = 0,
    val dueTodayCount: Int = 0,
    val expiredCount: Int = 0,
    val totalRiskValue: Long = 0L,
    val activeRiskValue: Long = 0L,
    val expiredRiskValue: Long = 0L,
    val missingPriceBatchesCount: Int = 0,
    val priorityBatches: List<StockBatch> = emptyList(),
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val getBatchesUseCase: GetBatchesUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = getBatchesUseCase(statusFilter = "AVAILABLE").map { availableList ->
        val normalList = availableList.filter { it.urgencyLevel() == UrgencyLevel.NORMAL }
        val attentionList = availableList.filter { it.urgencyLevel() == UrgencyLevel.ATTENTION }
        val nearList = availableList.filter { it.urgencyLevel() == UrgencyLevel.NEAR }
        val urgentList = availableList.filter { it.urgencyLevel() == UrgencyLevel.URGENT }
        val dueTodayList = availableList.filter { it.urgencyLevel() == UrgencyLevel.DUE_TODAY }
        val expiredList = availableList.filter { it.urgencyLevel() == UrgencyLevel.EXPIRED }

        val totalRisk = availableList.sumOf { it.remainingRiskValue() ?: 0L }
        val activeRisk = availableList.filter { it.daysLeft() >= 0 }.sumOf { it.remainingRiskValue() ?: 0L }
        val expiredRisk = availableList.filter { it.daysLeft() < 0 }.sumOf { it.remainingRiskValue() ?: 0L }

        val missingPriceCount = availableList.count { it.purchasePricePerUnit == null }
        val sortedPriority = availableList.sortedBy { it.expiryDate }

        DashboardUiState(
            totalAvailableCount = availableList.size,
            normalCount = normalList.size,
            attentionCount = attentionList.size,
            nearCount = nearList.size,
            urgentCount = urgentList.size,
            dueTodayCount = dueTodayList.size,
            expiredCount = expiredList.size,
            totalRiskValue = totalRisk,
            activeRiskValue = activeRisk,
            expiredRiskValue = expiredRisk,
            missingPriceBatchesCount = missingPriceCount,
            priorityBatches = sortedPriority,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )
}
