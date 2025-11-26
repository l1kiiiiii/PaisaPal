# ----------------------------------------------------------------------------
# DATA MODULE INTERNAL RULES
# ----------------------------------------------------------------------------
# These rules apply when the Data module code is being shrunk.

# Keep internal mappers that might use reflection
-keep class com.example.paisapal.data.mapper.** { *; }

# Keep Room entities for internal database operations
-keep class com.example.paisapal.data.local.entity.** { *; }