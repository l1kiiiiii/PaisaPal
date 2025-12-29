# ============================================================================
# DATA MODULE RULES
# ============================================================================

# 1. Keep Room Entities
# Room uses reflection to map these to database tables.
-keep class com.example.data.local.entity.** { *; }

# 2. Keep DAOs
# Room generates code for these; keeping the interface ensures mapping works.
-keep class com.example.data.local.dao.** { *; }

# 3. Keep Hilt Modules
# Ensures Dependency Injection wiring isn't stripped.
-keep class com.example.data.di.** { *; }

# 4. Keep Notification Cache Implementation
# Since this is used by the Service in the App module.
-keep class com.example.data.cache.NotificationCacheImpl { *; }

# 5. Keep Mappers (Safe default for Clean Architecture)
-keep class com.example.data.mapper.** { *; }