package com.example.paisapal.ui.screens.settings


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.MatchTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val matchTransactionsUseCase: MatchTransactionsUseCase
) : ViewModel() {

    private val _matchingState = MutableStateFlow<MatchingState>(MatchingState.Idle)
    val matchingState: StateFlow<MatchingState> = _matchingState.asStateFlow()

    fun matchTransactions() {
        viewModelScope.launch {
            _matchingState.value = MatchingState.Loading
            try {
                matchTransactionsUseCase.execute()
                _matchingState.value = MatchingState.Success
            } catch (e: Exception) {
                _matchingState.value = MatchingState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class MatchingState {
    object Idle : MatchingState()
    object Loading : MatchingState()
    object Success : MatchingState()
    data class Error(val message: String) : MatchingState()
}
