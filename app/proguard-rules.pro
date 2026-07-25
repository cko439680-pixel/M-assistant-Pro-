# Add project specific ProGuard rules here.

# Keep Compose reflection and entry points
-keepclassmembers class * extends androidx.compose.ui.node.Owner { *; }

# Keep Model data classes and Serialized names
-keepclassmembers class com.example.data.** { *; }

# Strip debug logging calls for release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep Accessibility Service & Foreground Overlay Service
-keep class com.example.service.GameAccessibilityService { *; }
-keep class com.example.service.GameAssistantService { *; }

# Optimize DEX bytecode & shrink resources
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses

