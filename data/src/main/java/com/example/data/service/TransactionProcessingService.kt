package com.example.data.service

import android.content.Context
import com.example.data.context.ContextGatherer
import com.example.data.local.TransactionDao
import com.example.data.mapper.toEntity
import com.example.data.notification.LearningNotificationHelper
import com.example.domain.engine.ContextAwareCategorizer
import com.example.domain.model.Transaction
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionProcessingService(
    private val contextGatherer: ContextGatherer,
    private val categorizer: ContextAwareCategorizer,
    private val transactionDao: TransactionDao,
    private val context: Context
) {

    private val gson = Gson()
    private val processingScope = CoroutineScope(Dispatchers.IO)

    fun processTransaction(transaction: Transaction) {
        processingScope.launch {
            try {
                // Step 1: Gather context
                val contextSnapshot = contextGatherer.gatherContext(
                    transactionId = transaction.id,
                    rawSms = transaction.smsBody,
                    timestamp = transaction.timestamp,
                    amount = transaction.amount,
                    upiVpa = transaction.upiVpa
                )

                // Step 2: Categorize using context
                val categoryResult = categorizer.categorize(contextSnapshot)

                // Step 3: Update transaction with category and context
                val updatedTransaction = transaction.copy(
                    category = categoryResult.category,
                    needsReview = categoryResult.confidence < 0.8f
                )

                val entity = updatedTransaction.toEntity().copy(
                    contextData = gson.toJson(contextSnapshot)
                )

                // Step 4: Save to database
                transactionDao.update(entity)

                // Step 5: Handle learning if needed
                if (categoryResult.metadata["needsLearning"] == "true") {
                    triggerLearningNotification(transaction, contextSnapshot)
                }

            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }

    private fun triggerLearningNotification(
        transaction: Transaction,
        contextSnapshot: com.example.domain.model.ContextSnapshot
    ) {
        val merchantInfo = contextSnapshot.strongestBluetoothDevice?.deviceName
            ?: contextSnapshot.foregroundAppPackage
            ?: "Unknown"

        val notificationHelper = LearningNotificationHelper(context)
        notificationHelper.showLearningPrompt(transaction.id, merchantInfo)
    }
}
