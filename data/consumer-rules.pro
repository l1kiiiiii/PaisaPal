# ----------------------------------------------------------------------------
# DATA MODULE EXPORTED RULES
# ----------------------------------------------------------------------------
# These rules are automatically applied to any App that depends on this module.

# ROOM DATABASE
# Required so the database can find table names and columns via reflection
-keep class com.example.paisapal.data.local.entity.** { *; }

# SQLCIPHER (Encryption)
# Required to prevent stripping of native bridge classes used for database encryption
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# DATA TRANSFER OBJECTS (Network)
# Required if you use Gson/Retrofit to parse JSON into these classes
-keep class com.example.paisapal.data.remote.dto.** { *; }