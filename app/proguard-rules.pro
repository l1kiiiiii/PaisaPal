# ============================================================================
# APP MODULE RULES
# ============================================================================
# 1. Keep App Components
-keep class com.example.paisapal.receiver.SmsReceiver { *; }
-keep class com.example.paisapal.service.** { *; }
-keep class com.example.paisapal.worker.** { *; }
-keep class com.example.paisapal.di.** { *; }
-keep class com.example.paisapal.util.SmsReader { *; }

# ============================================================================
# DOMAIN MODULE RULES (Moved from domain - pure Kotlin lib can't export)
# ============================================================================
# Keep Domain Models
-keep class com.example.domain.model.** { *; }
-keep class com.example.domain.data.** { *; }

# Keep Domain Engines and Strategies
-keep class com.example.domain.engine.** { *; }
-keep class com.example.domain.strategy.** { *; }

# Keep Repository Interfaces
-keep interface com.example.domain.repository.** { *; }

# ============================================================================
# DATA MODULE INTEGRATION (Critical for new cache architecture)
# ============================================================================
# Keep NotificationCache interface and implementation
-keep interface com.example.domain.data.NotificationCache { *; }
-keep class com.example.domain.data.PaymentNotification { *; }
-keep class com.example.data.cache.NotificationCacheImpl { *; }

# Keep Repository implementations
-keep class com.example.data.repository.** { *; }

# Keep Hilt modules
-keep class com.example.data.di.** { *; }

# ============================================================================
# HILT DEPENDENCY INJECTION
# ============================================================================
# Keep Hilt-generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Keep injected members
-keepclassmembers class * {
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <init>(...);
}

# ============================================================================
# ANDROID SYSTEM COMPONENTS
# ============================================================================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep Notification API classes
-keep class android.service.notification.StatusBarNotification {
    public android.app.Notification getNotification();
    public java.lang.String getPackageName();
    public long getPostTime();
}
-keep class android.app.Notification {
    public android.os.Bundle extras;
}
-keep class android.os.Bundle {
    public java.lang.CharSequence getCharSequence(java.lang.String);
}

# ============================================================================
# KOTLIN COROUTINES
# ============================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ============================================================================
# COMPOSE
# ============================================================================
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn androidx.compose.**