# ============================================================================
# APP MODULE - KEEP RULES
# ============================================================================
# Purpose: Protect Android entry points (Activities, Receivers, Services).
# Package structure: com.example.paisapal.*

# 1. Keep Critical Broadcast Receivers
# The SmsReceiver is instantiated by the system via Manifest.
-keep class com.example.paisapal.receiver.SmsReceiver { *; }

# 2. Keep Services
# NotificationMonitorService and others.
-keep class com.example.paisapal.service.** { *; }

# 3. Keep Utility Classes used in Background work
# SmsReader is used via Hilt injection.
-keep class com.example.paisapal.util.SmsReader { *; }
-keep class com.example.paisapal.worker.** { *; }

# 4. Hilt / Dependency Injection Safety
# Generally handled by Hilt's own rules, but these ensure your specific injection points exist.
-keep class com.example.paisapal.di.** { *; }

# 5. Android Standard Components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# 6. Keep Android Notification classes (from your original file)
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