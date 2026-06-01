package io.github.alimsrepo.navease.runtime.annotations

/**
 * Marks an [io.github.alimsrepo.navease.runtime.presentation.ActivityScreen] subclass for
 * automatic registration.
 *
 * The NavEase KSP processor discovers every `@AutoRegister` class and generates:
 *
 * 1. **`AutoRegisterScreens.kt`** containing:
 *    - `private object NavEaseAutoInit` — registers all screens into the global
 *      [io.github.alimsrepo.navease.runtime.presentation.NavEaseAutoRegistry]
 *      so the zero-configuration
 *      [io.github.alimsrepo.navease.runtime.presentation.NavEaseHost] overload works.
 *    - `fun NavEaseScreenScope<Root>.autoRegisterScreens()` — registers all screens
 *      into a typed scope for the explicit `NavEaseHost<Root>(start = ...) { }` overload.
 *
 * Both the zero-arg [io.github.alimsrepo.navease.runtime.presentation.NavEaseHost] **and**
 * [io.github.alimsrepo.navease.runtime.presentation.autoRegisterScreens] live in the
 * **runtime library** — so your code compiles cleanly in the IDE **before** KSP runs.
 * After the first build, the generated init logic wires everything together automatically.
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
 * // Root composable — two equivalent options:
 *
 * // Option A — universal (auto-discovers screens; works out of the box on Android/JVM):
 * @Composable fun App() {
 *     NavEaseHost(onExitRequest = { finish() })
 * }
 *
 * // Option B — typed (works on every platform, recommended for KMP projects):
 * @Composable fun App() {
 *     NavEaseHost<AppScreens>(start = AppScreens.Splash, onExitRequest = { finish() }) {
 *         autoRegisterScreens()   // no-op stub before KSP; real impl generated after
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
 *                         entry by the [io.github.alimsrepo.navease.runtime.presentation.NavEaseHost]
 *                         overloads. Exactly one screen per module should set this to `true`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoRegister(val startDestination: Boolean = false)


