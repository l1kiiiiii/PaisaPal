package com.example.domain.model


data class ContextSignature(
    val id: String,
    val triggerType: TriggerType,
    val triggerValue: String,
    val learnedCategory: String,
    val confidenceScore: Float,
    val hitCount: Int
)
