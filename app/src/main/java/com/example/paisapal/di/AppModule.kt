package com.example.paisapal.di

import com.example.domain.engine.CategorizationEngine
import com.example.domain.engine.ContextEngine
import com.example.domain.engine.SmsProcessingEngine
import com.example.domain.engine.TransactionMatchingEngine
import com.example.domain.engine.TransactionParser
import com.example.domain.repository.BudgetRepository
import com.example.domain.repository.NotificationRepository
import com.example.domain.repository.SavedPlaceRepository
import com.example.domain.repository.TransactionRepository
import com.example.domain.usecase.CheckBudgetAlertsUseCase
import com.example.domain.usecase.GetBudgetSummaryUseCase
import com.example.domain.usecase.GetInsightsUseCase
import com.example.domain.usecase.MatchTransactionsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTransactionParser(): TransactionParser {
        return TransactionParser()
    }

    @Provides
    @Singleton
    fun provideCategorizationEngine(): CategorizationEngine {
        return CategorizationEngine()
    }

    @Provides
    @Singleton
    fun provideContextEngine(
        notificationRepository: NotificationRepository,
        savedPlaceRepository: SavedPlaceRepository
    ): ContextEngine {
        return ContextEngine(
            notificationRepository = notificationRepository,
            savedPlaceRepository = savedPlaceRepository
        )
    }

    @Provides
    @Singleton
    fun provideTransactionMatchingEngine(
        repository: TransactionRepository
    ): TransactionMatchingEngine {
        return TransactionMatchingEngine(repository)
    }

    @Provides
    @Singleton
    fun provideMatchTransactionsUseCase(
        matchingEngine: TransactionMatchingEngine
    ): MatchTransactionsUseCase {
        return MatchTransactionsUseCase(matchingEngine)
    }

    @Provides
    @Singleton
    fun provideSmsProcessingEngine(
        transactionParser: TransactionParser,
        categorizationEngine: CategorizationEngine,
        contextEngine: ContextEngine,
        transactionRepository: TransactionRepository
    ): SmsProcessingEngine {
        return SmsProcessingEngine(
            transactionParser = transactionParser,
            categorizationEngine = categorizationEngine,
            contextEngine = contextEngine,
            transactionRepository = transactionRepository
        )
    }

    @Provides
    @Singleton
    fun provideGetInsightsUseCase(
        repository: TransactionRepository
    ): GetInsightsUseCase {
        return GetInsightsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetBudgetSummaryUseCase(
        budgetRepository: BudgetRepository,
        transactionRepository: TransactionRepository
    ): GetBudgetSummaryUseCase {
        return GetBudgetSummaryUseCase(budgetRepository, transactionRepository)
    }

    @Provides
    @Singleton
    fun provideCheckBudgetAlertsUseCase(
        getBudgetSummaryUseCase: GetBudgetSummaryUseCase
    ): CheckBudgetAlertsUseCase {
        return CheckBudgetAlertsUseCase(getBudgetSummaryUseCase)
    }
}
