package com.example.domain.model


data class CategoryResult(
    val category: String,
    val confidence: Float,
    val strategy: CategorizationStrategy,
    val metadata: Map<String, String> = emptyMap()
)

enum class CategorizationStrategy {
    ACTIVE_APP,
    SOUNDBOX,
    IDENTITY_VPA,
    PATTERN_HEURISTIC,
    TEXT_FALLBACK,
    UNCATEGORIZED
}
