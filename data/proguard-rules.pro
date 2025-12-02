# ----------------------------------------------------------------------------
# DATA MODULE INTERNAL RULES (for library itself)
# ----------------------------------------------------------------------------
# Keep all data classes
-keep class com.example.data.** { *; }

# Room - Keep generated implementations
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**