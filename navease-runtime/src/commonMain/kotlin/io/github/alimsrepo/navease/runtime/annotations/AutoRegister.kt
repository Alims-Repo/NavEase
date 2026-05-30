package io.github.alimsrepo.navease.runtime.annotations

/**
 * Marks an [io.github.alimsrepo.navease.runtime.presentation.ActivityScreen] subclass for
 * automatic registration inside [io.github.alimsrepo.navease.runtime.presentation.NavEaseHost].
 *
 * The NavEase KSP processor discovers every class annotated with `@AutoRegister`, validates
 * that no two screens share the same NavKey type, and generates a single extension function:
 *
 * ```kotlin
 * fun NavEaseScreenScope<AppScreens>.autoRegisterScreens() {
 *     add(SplashScreen())
 *     add(HomeScreen())
 *     // ... every @AutoRegister screen
 * }
 * ```
 *
 * Use it in your root composable instead of listing screens manually:
 *
 * ```kotlin
 * NavEaseHost<AppScreens>(start = AppScreens.Splash) {
 *     autoRegisterScreens()   // ← one line, auto-updated on rebuild
 * }
 * ```
 *
 * **Constraints enforced at compile time:**
 * - Each NavKey type (`K`) may only be handled by **one** `@AutoRegister` screen.
 *   Duplicate registrations produce a KSP error.
 * - All `@AutoRegister` screens in a module must share the **same** sealed root NavKey class.
 *   Mixed roots produce a KSP error.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoRegister

