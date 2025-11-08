package com.example.domain.usecase

import com.example.domain.engine.TransactionMatchingEngine

class MatchTransactionsUseCase(
    private val matchingEngine: TransactionMatchingEngine
) {

    suspend fun execute() {
        // Step 1: Match by reference number
        matchingEngine.matchTransactionsByReference()

        // Step 2: Match by amount and time
        matchingEngine.matchTransactionsByAmountAndTime()
    }
}
