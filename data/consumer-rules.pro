# ----------------------------------------------------------------------------
# DATA MODULE CONSUMER RULES
# ----------------------------------------------------------------------------
# These rules are automatically applied to modules that depend on :data

# ============================================================================
# 1. NEW NOTIFICATION CACHE ARCHITECTURE (Critical Fix)
# ============================================================================
# Protect the cache implementation and repository
-keep class com.example.data.cache.** { *; }
-keep class com.example.data.repository.** { *; }

# ============================================================================
# 2. ROOM DATABASE (Fixed Package Name)
# ============================================================================
# Was: com.example.paisapal.data.local.entity.**
# Fixed: com.example.data.local.entity.**
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.data.local.dao.** { *; }

# Room - Prevent obfuscation of database structure
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ============================================================================
# 3. SQLCIPHER (Database Encryption)
# ============================================================================
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# ============================================================================
# 4. DATA TRANSFER OBJECTS (Network Layer)
# ============================================================================
# Was: com.example.paisapal.data.remote.dto.**
# Fixed: com.example.data.remote.dto.**
# (Only needed if you have a remote package)
-keep class com.example.data.remote.dto.** { *; }

# ============================================================================
# 5. HILT DEPENDENCY INJECTION
# ============================================================================
# Protect Hilt modules in data layer
-keep class com.example.data.di.** { *; }

# Prevent stripping of @Inject constructors
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# ============================================================================
# 6. GSON/JSON SERIALIZATION (if used)
# ============================================================================
# Prevent field name obfuscation for JSON parsing
-keepclassmembers class com.example.data.** {
    @com.google.gson.annotations.SerializedName <fields>;
}
