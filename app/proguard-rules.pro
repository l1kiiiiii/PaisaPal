# ===========================================
# NOTIFICATION LISTENER - CRITICAL
# ===========================================
-keep public class * extends android.service.notification.NotificationListenerService {
    public void onNotificationPosted(android.service.notification.StatusBarNotification);
    public void onNotificationRemoved(android.service.notification.StatusBarNotification);
    public void onListenerConnected();
    public void onListenerDisconnected();
}

# Keep the Service itself
-keep class com.example.paisapal.service.NotificationMonitorService { *; }

# Keep Android Notification classes used in the service
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

# ===========================================
# DOMAIN & DATA CLASSES (CORRECTED PACKAGES)
# ===========================================
# Fix: Removed ".paisapal" from these paths to match your actual file structure

-keep class com.example.domain.data.AppRegistry { *; }
-keep class com.example.domain.data.NotificationCache { *; }

# Keep the Implementation so Hilt can inject it
-keep class com.example.data.cache.NotificationCacheImpl { *; }

# Keep Domain Models & Engines
-keep class com.example.domain.model.** { *; }
-keep class com.example.domain.engine.** { *; }

# Keep Room Entities
-keep class com.example.data.local.entity.** { *; }