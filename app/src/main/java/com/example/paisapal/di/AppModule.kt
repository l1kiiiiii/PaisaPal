// app/src/main/java/com/example/paisapal/di/AppModule.kt
package com.example.paisapal.di

import com.example.domain.data.NotificationCache
import com.example.domain.engine.*
import com.example.domain.repository.*
import com.example.domain.usecase.*
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
    fun provideCategorizationEngine(
        userCorrectionRepository: UserCorrectionRepository  // ADD THIS
    ): CategorizationEngine {
        return CategorizationEngine(userCorrectionRepository)
    }

    @Provides
    @Singleton
    fun provideSenderAuthentication(): SenderAuthentication {
        return SenderAuthentication()
    }

    @Provides
    @Singleton
    fun provideContextEngine(
        notificationCache: NotificationCache,
        savedPlaceRepository: SavedPlaceRepository,
        locationProvider: LocationProvider
    ): ContextEngine {
        return ContextEngine(
            notificationCache = notificationCache,
            savedPlaceRepository = savedPlaceRepository,
            locationProvider = locationProvider
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
        senderAuthentication: SenderAuthentication,
        transactionParser: TransactionParser,
        categorizationEngine: CategorizationEngine,
        contextEngine: ContextEngine,

        userAccountsRepository: UserAccountsRepository
    ): SmsProcessingEngine {
        return SmsProcessingEngine(
            senderAuthentication = senderAuthentication,
            transactionParser = transactionParser,
            categorizationEngine = categorizationEngine,
            contextEngine = contextEngine,
            userAccountsRepository = userAccountsRepository
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
