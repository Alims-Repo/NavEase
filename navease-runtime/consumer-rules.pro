# NavEase consumer ProGuard/R8 rules
# These rules are applied automatically to any Android app that depends on navease-runtime.

# ── NavScreen subclasses ────────────────────────────────────────────────────
# KSP-generated ScreenFactory instantiates NavScreen subclasses by name via a
# when() expression. R8 must not remove or rename them.
-keep class * extends io.github.alimsrepo.navease.runtime.domain.NavScreen { *; }

# ── KSP-generated classes ───────────────────────────────────────────────────
# The generated AppScreens sealed class and its subclasses are used as NavKey
# instances in the back stack and must be kept for serialization and navigation.
-keep class io.github.alimsrepo.navease.generated.** { *; }

# ── NavController ───────────────────────────────────────────────────────────
-keep class io.github.alimsrepo.navease.runtime.navigation.NavController { *; }

# ── kotlinx.serialization ───────────────────────────────────────────────────
# AppScreens subclasses are @Serializable — keep their serializers so the
# SavedStateConfiguration-backed back stack survives process death.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class io.github.alimsrepo.navease.generated.** {
    kotlinx.serialization.KSerializer serializer(...);
}

