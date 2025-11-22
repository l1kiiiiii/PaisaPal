package com.example.domain.strategy

import com.example.domain.model.CategoryResult
import com.example.domain.model.CategorizationStrategy
import com.example.domain.model.ContextSnapshot
import java.util.*

class PatternStrategy {

    suspend fun categorize(snapshot: ContextSnapshot): CategoryResult {
        val amount = snapshot.amount
        val calendar = Calendar.getInstance().apply {
            timeInMillis = snapshot.timestamp
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        // Pattern 1: Small amounts during tea/snack times
        if (amount < 50 && (hour in 9..11 || hour in 16..18)) {
            return CategoryResult(
                category = "Food",
                confidence = 0.6f,
                strategy = CategorizationStrategy.PATTERN_HEURISTIC,
                metadata = mapOf("pattern" to "tea_time")
            )
        }

        // Pattern 2: Large amounts early in month (rent/bills)
        if (amount > 10000 && dayOfMonth in 1..5) {
            return CategoryResult(
                category = "Bills",
                confidence = 0.65f,
                strategy = CategorizationStrategy.PATTERN_HEURISTIC,
                metadata = mapOf("pattern" to "monthly_bill")
            )
        }

        // Pattern 3: Medium amounts during lunch/dinner
        if (amount in 100.0..500.0 && (hour in 12..14 || hour in 19..21)) {
            return CategoryResult(
                category = "Food",
                confidence = 0.55f,
                strategy = CategorizationStrategy.PATTERN_HEURISTIC,
                metadata = mapOf("pattern" to "meal_time")
            )
        }

        // Pattern 4: Evening entertainment
        if (amount in 200.0..1000.0 && hour in 18..23) {
            return CategoryResult(
                category = "Entertainment",
                confidence = 0.5f,
                strategy = CategorizationStrategy.PATTERN_HEURISTIC,
                metadata = mapOf("pattern" to "evening_activity")
            )
        }

        return CategoryResult(
            category = "Uncategorized",
            confidence = 0.0f,
            strategy = CategorizationStrategy.PATTERN_HEURISTIC
        )
    }
}
