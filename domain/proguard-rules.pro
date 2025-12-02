# ============================================================================
# DOMAIN MODULE - KEEP RULES
# ============================================================================
# Purpose: Protect business logic, models, and engines from obfuscation.
# Package structure: com.example.domain.*

# 1. Keep all Domain Models
# Essential for passing data between layers without property renaming issues.
-keep class com.example.domain.model.** { *; }

# 2. Keep Domain Engines
# Critical for SmsProcessingEngine to function correctly via reflection/injection.
-keep class com.example.domain.engine.** { *; }

# 3. Keep Repositories Interfaces
# Ensures Hilt/Dagger can correctly link interfaces to implementations.
-keep interface com.example.domain.repository.** { *; }

# 4. Keep Strategy Implementations (if used dynamically)
-keep class com.example.domain.strategy.** { *; }