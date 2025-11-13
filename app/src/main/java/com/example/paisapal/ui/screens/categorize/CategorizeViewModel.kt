package com.example.paisapal.ui.screens.categorize

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategorizeViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    fun categorizeTransaction(transactionId: String, category: String) {
        viewModelScope.launch {
            try {
                repository.updateTransactionCategory(transactionId, category)
                Log.d(TAG, "Transaction categorized: $category")
            } catch (e: Exception) {
                Log.e(TAG, "Error categorizing", e)
            }
        }
    }

    companion object {
        private const val TAG = "CategorizeViewModel"
    }
}
