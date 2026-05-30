package io.github.alimsrepo.navease.runtime.annotations

/**
 * Marks an [io.github.alimsrepo.navease.runtime.presentation.ActivityScreen] subclass for
 * automatic registration — and optionally designates it as the start destination.
 *
 * The NavEase KSP processor discovers every `@AutoRegister` class and generates two things:
 *
 * 1. `fun NavEaseScreenScope<Root>.autoRegisterScreens()` — registers all screens.
 * 2. `@Composable fun NavEaseHost(onExitRequest, enableSharedTransitions, navTransition)` —
 *    a zero-boilerplate host that bakes in `autoRegisterScreens()` and the start destination.
 *
 * ## Usage
 *
 * ```kotlin
 * // Mark the start screen:
 * @AutoRegister(startDestination = true)
 * class SplashScreen : ActivityScreen<AppScreens.Splash>() { ... }
 *
 * // Mark every other screen (no args needed):
 * @AutoRegister
 * class HomeScreen : ActivityScreen<AppScreens.Home>() { ... }
 *
 * // Root composable — nothing to list manually:
 * @Composable fun App() {
 *     MaterialTheme {
 *         NavEaseHost(onExitRequest = { finish() })
 *     }
 * }
 * ```
 *
 * **Constraints enforced at compile time by the KSP processor:**
 * - Each NavKey type may only be handled by **one** `@AutoRegister` screen (duplicate → error).
 * - All screens must share the **same** sealed root NavKey class (mixed roots → error).
 * - Exactly **one** screen may have `startDestination = true` (more than one → error).
 *
 * @param startDestination When `true`, this screen's NavKey is used as the initial back-stack
 *                         entry in the generated [NavEaseHost] overload. Exactly one screen
 *                         per module should set this to `true`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoRegister(val startDestination: Boolean = false)


