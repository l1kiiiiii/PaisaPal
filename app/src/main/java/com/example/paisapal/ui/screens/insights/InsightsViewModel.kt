package com.example.paisapal.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.InsightsData
import com.example.domain.usecase.GetInsightsUseCase
import com.example.domain.usecase.InsightsPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getInsightsUseCase: GetInsightsUseCase
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(InsightsPeriod.MONTHLY)
    val selectedPeriod: StateFlow<InsightsPeriod> = _selectedPeriod.asStateFlow()

    private val _insightsData = MutableStateFlow<InsightsData?>(null)
    val insightsData: StateFlow<InsightsData?> = _insightsData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadInsights()
    }

    fun selectPeriod(period: InsightsPeriod) {
        _selectedPeriod.value = period
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = getInsightsUseCase.execute(_selectedPeriod.value)
                _insightsData.value = data
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
