# ----------------------------------------------------------------------------
# APP MODULE RULES
# ----------------------------------------------------------------------------


# 1. Keep Domain Data (AppRegistry, NotificationCache)
# Your code is in 'com.example.domain', NOT 'com.example.paisapal.domain'
-keep class com.example.domain.data.** { *; }
-keep class com.example.domain.model.** { *; }

# 2. Keep Data Implementation (NotificationCacheImpl)
# Critical for Hilt injection to work in Release
-keep class com.example.data.cache.** { *; }

# 3. Keep Data Entities (Room)
-keep class com.example.data.local.entity.** { *; }

# 4. Keep Hilt/Dagger (Standard)
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }