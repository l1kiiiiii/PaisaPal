package com.example.data.di


import com.example.domain.engine.ContextAwareCategorizer
import com.example.domain.repository.ContextSignatureRepository
import com.example.domain.strategy.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CategorizationModule {

    @Provides
    @Singleton
    fun provideActiveAppStrategy(): ActiveAppStrategy {
        return ActiveAppStrategy()
    }

    @Provides
    @Singleton
    fun provideSoundBoxStrategy(): SoundBoxStrategy {
        return SoundBoxStrategy()
    }

    @Provides
    @Singleton
    fun provideIdentityStrategy(): IdentityStrategy {
        return IdentityStrategy()
    }

    @Provides
    @Singleton
    fun providePatternStrategy(): PatternStrategy {
        return PatternStrategy()
    }

    @Provides
    @Singleton
    fun provideTextStrategy(): TextStrategy {
        return TextStrategy()
    }

    @Provides
    @Singleton
    fun provideContextAwareCategorizer(
        signatureRepository: ContextSignatureRepository,
        activeAppStrategy: ActiveAppStrategy,
        soundBoxStrategy: SoundBoxStrategy,
        identityStrategy: IdentityStrategy,
        patternStrategy: PatternStrategy,
        textStrategy: TextStrategy
    ): ContextAwareCategorizer {
        return ContextAwareCategorizer(
            signatureRepository = signatureRepository,
            activeAppStrategy = activeAppStrategy,
            soundBoxStrategy = soundBoxStrategy,
            identityStrategy = identityStrategy,
            patternStrategy = patternStrategy,
            textStrategy = textStrategy
        )
    }
}
