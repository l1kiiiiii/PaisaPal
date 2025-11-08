package com.example.paisapal.worker


import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.domain.usecase.MatchTransactionsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TransactionMatchWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val matchTransactionsUseCase: MatchTransactionsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            matchTransactionsUseCase.execute()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
