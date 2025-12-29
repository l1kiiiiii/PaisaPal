# ============================================================================
# DOMAIN MODULE RULES
# ============================================================================

# 1. Keep Domain Models
# These are used by Room (Data layer) and UI. If they are renamed, the app crashes.
-keep class com.example.domain.model.** { *; }

# 2. Keep Repository Interfaces
# Needed for Hilt to bind implementations correctly.
-keep interface com.example.domain.repository.** { *; }

# 3. Keep Engines (Optional, but safer for "Brains" of the app)
-keep class com.example.domain.engine.** { *; }