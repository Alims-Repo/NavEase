# NavEase sample app ProGuard/R8 rules
# Consumer rules for navease-runtime are automatically applied via consumer-rules.pro.
# Add any app-specific rules here.

# Keep Kotlin metadata for reflection (required by kotlinx.serialization)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep Compose-generated code
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

