# ------------------- ProGuard Rules for AppointmentNow -------------------
# Optimized by: Conor Steward
# Last Updated: 03/07/25
#
# Description:
# This file contains ProGuard rules for obfuscating, shrinking, and optimizing
# the AppointmentNow Android application. It helps reduce APK size and 
# protects the code while maintaining required functionality.
#
# Reference:
# - Official Documentation: https://developer.android.com/studio/build/shrink-code
# ------------------------------------------------------------------------

# Keep Application Classes (Avoid Stripping Main App Code)
-keep class com.example.appointmentnow_steward.** { *; }

# Keep Parcelable classes to ensure Android can properly serialize them
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep all custom model classes (Replace "model" with the correct package if needed)
-keep class com.example.appointmentnow_steward.model.** { *; }

# Keep AndroidX and Material Components (Prevents UI issues)
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }

# Keep Database Entities and Helper Methods
-keep class * extends android.database.sqlite.SQLiteOpenHelper { *; }
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# Keep JSON Parsing Classes (For Gson or other libraries)
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Methods for Reflection-Based Access
-keepclassmembers class * {
    @java.lang.reflect.* <methods>;
}

# Keep WebView JavaScript Interfaces (Uncomment if needed)
# -keepclassmembers class fqcn.of.javascript.interface.for.webview {
#    public *;
# }

# Keep Logging Messages in Debug Mode Only
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Preserve Line Number Information (For Better Debugging)
-keepattributes SourceFile,LineNumberTable

# Rename Source File Attribute (Obfuscates Original File Names)
-renamesourcefileattribute "HiddenSource"