package com.example.domain.engine

import com.example.domain.model.Transaction
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first

class TransactionMatchingEngine(
    private val repository: TransactionRepository
) {

    suspend fun matchTransactionsByReference() {
        val allTransactions = repository.getAllTransactions().first()
        val matchedPairs = mutableListOf<Pair<Transaction, Transaction>>()

        val byRefNumber = allTransactions
            .filter { it.referenceNumber != null }
            .groupBy { it.referenceNumber!! }

        byRefNumber.forEach { (refNo, transactions) ->
            if (transactions.size >= 2) {
                val bankTxn = transactions.find { isBankSms(it) }
                val upiTxn = transactions.find { isUpiSms(it) && it.merchantDisplayName != null }

                if (bankTxn != null && upiTxn != null) {
                    matchedPairs.add(Pair(bankTxn, upiTxn))
                }
            }
        }

        matchedPairs.forEach { (bankTxn, upiTxn) ->
            mergeTransactions(bankTxn, upiTxn)
        }
    }

    suspend fun matchTransactionsByAmountAndTime() {
        val allTransactions = repository.getAllTransactions().first()
        val matchedPairs = mutableListOf<Pair<Transaction, Transaction>>()

        for (i in allTransactions.indices) {
            for (j in i + 1 until allTransactions.size) {
                val txn1 = allTransactions[i]
                val txn2 = allTransactions[j]

                if (isSimilarTransaction(txn1, txn2)) {
                    val primary = if (hasMoreInfo(txn1, txn2)) txn1 else txn2
                    val secondary = if (hasMoreInfo(txn1, txn2)) txn2 else txn1

                    matchedPairs.add(Pair(primary, secondary))
                }
            }
        }

        matchedPairs.forEach { (primary, secondary) ->
            mergeAndDeleteDuplicate(primary, secondary)
        }
    }

    private suspend fun mergeTransactions(bankTxn: Transaction, upiTxn: Transaction) {
        val merged = bankTxn.copy(
            merchantRaw = upiTxn.merchantRaw ?: bankTxn.merchantRaw,
            merchantDisplayName = upiTxn.merchantDisplayName ?: bankTxn.merchantDisplayName,
            upiVpa = upiTxn.upiVpa ?: bankTxn.upiVpa,
            category = upiTxn.category ?: bankTxn.category,
            needsReview = false
        )

        repository.update(merged)

        if (shouldDeleteDuplicate(bankTxn, upiTxn)) {
            repository.delete(upiTxn)
        }
    }

    private suspend fun mergeAndDeleteDuplicate(primary: Transaction, secondary: Transaction) {
        val merged = primary.copy(
            merchantRaw = primary.merchantRaw ?: secondary.merchantRaw,
            merchantDisplayName = primary.merchantDisplayName ?: secondary.merchantDisplayName,
            upiVpa = primary.upiVpa ?: secondary.upiVpa,
            category = primary.category ?: secondary.category,
            referenceNumber = primary.referenceNumber ?: secondary.referenceNumber,
            needsReview = false
        )

        repository.update(merged)
        repository.delete(secondary)
    }

    private fun isBankSms(transaction: Transaction): Boolean {
        val bankKeywords = listOf("account", "a/c", "credited", "debited", "balance")
        return bankKeywords.any { transaction.smsBody.contains(it, ignoreCase = true) }
    }

    private fun isUpiSms(transaction: Transaction): Boolean {
        val upiKeywords = listOf("upi", "gpay", "phonepe", "paytm", "paid to", "received from")
        return upiKeywords.any { transaction.smsBody.contains(it, ignoreCase = true) }
    }

    private fun isSimilarTransaction(txn1: Transaction, txn2: Transaction): Boolean {
        if (txn1.amount != txn2.amount) return false

        val timeDiff = kotlin.math.abs(txn1.timestamp - txn2.timestamp)
        if (timeDiff > 5 * 60 * 1000) return false

        val oneIsBankOnce = (isBankSms(txn1) && isUpiSms(txn2)) || (isUpiSms(txn1) && isBankSms(txn2))
        if (!oneIsBankOnce) return false

        return true
    }

    private fun hasMoreInfo(txn1: Transaction, txn2: Transaction): Boolean {
        var score1 = 0
        var score2 = 0

        if (txn1.merchantDisplayName != null) score1 += 3
        if (txn1.upiVpa != null) score1 += 2
        if (txn1.category != null) score1 += 1

        if (txn2.merchantDisplayName != null) score2 += 3
        if (txn2.upiVpa != null) score2 += 2
        if (txn2.category != null) score2 += 1

        return score1 >= score2
    }

    private fun shouldDeleteDuplicate(bankTxn: Transaction, upiTxn: Transaction): Boolean {
        val sameAmount = bankTxn.amount == upiTxn.amount
        val sameRef = bankTxn.referenceNumber == upiTxn.referenceNumber
        val timeDiff = kotlin.math.abs(bankTxn.timestamp - upiTxn.timestamp)
        val withinTime = timeDiff < 10 * 60 * 1000

        return sameAmount && sameRef && withinTime
    }
}
