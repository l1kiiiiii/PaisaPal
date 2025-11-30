# ============================================================================
# DATA MODULE - KEEP RULES
# ============================================================================
# Purpose: Protect database entities, DTOs, and repository implementations.
# Package structure: com.example.data.*

# 1. Keep Room Entities
# CRITICAL: Room requires field names to match database column names.
-keep class com.example.data.local.entity.** { *; }

# 2. Keep Data Transfer Objects (DTOs) / Mappers
# Prevents issues if mapping uses reflection or serialization.
-keep class com.example.data.mapper.** { *; }

# 3. Keep Repository Implementations
# Necessary for Dependency Injection to instantiate these classes.
-keep class com.example.data.repository.** { *; }

# 4. Keep Local Data Sources (DAO implementations are handled by Room, but keep interfaces)
-keep class com.example.data.local.dao.** { *; }