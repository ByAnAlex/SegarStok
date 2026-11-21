package com.segarstok.feature.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segarstok.domain.recommendation.BatchRecommendation
import com.segarstok.domain.usecase.GetRecommendationsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class RecommendationUiState(
    val recommendations: List<BatchRecommendation> = emptyList(),
    val selectedDiscountRate: Double = 0.10,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendationViewModel(
    private val getRecommendationsUseCase: GetRecommendationsUseCase
) : ViewModel() {

    private val _selectedDiscountRate = MutableStateFlow(0.10)
    val selectedDiscountRate: StateFlow<Double> = _selectedDiscountRate.asStateFlow()

    val uiState: StateFlow<RecommendationUiState> = _selectedDiscountRate.flatMapLatest { rate ->
        getRecommendationsUseCase(discountRate = rate).map { list ->
            RecommendationUiState(
                recommendations = list,
                selectedDiscountRate = rate,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecommendationUiState(isLoading = true)
    )

    fun onDiscountRateChange(newRate: Double) {
        _selectedDiscountRate.value = newRate
    }
}
