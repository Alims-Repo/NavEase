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
 *      so the auto-discover
 *      [io.github.alimsrepo.navease.runtime.presentation.NavEaseHost] overload works.
 *    - `fun NavEaseScreenScope<Root>.autoRegisterScreens()` — registers all screens
 *      into a typed scope for the explicit `NavEaseHost<Root>(start = ...) { }` overload.
 *
 * Both the auto-discover [io.github.alimsrepo.navease.runtime.presentation.NavEaseHost] **and**
 * [io.github.alimsrepo.navease.runtime.presentation.autoRegisterScreens] live in the
 * **runtime library** — so your code compiles cleanly in the IDE **before** KSP runs.
 * After the first build, the generated init logic wires everything together automatically.
 *
 * The start destination is **not** declared on the annotation — it is declared explicitly
 * at the host level via the `start` parameter of [io.github.alimsrepo.navease.runtime.host.NavEaseHost].
 * This allows nested [NavEaseHost][io.github.alimsrepo.navease.runtime.host.NavEaseHost]
 * instances to each declare their own start destination independently.
 *
 * ## Usage
 *
 * ```kotlin
 * // Annotate every screen — no arguments needed:
 * @AutoRegister
 * class SplashScreen : ActivityScreen<AppScreens.Splash>() { ... }
 *
 * @AutoRegister
 * class HomeScreen : ActivityScreen<AppScreens.Home>() { ... }
 *
 * // Root composable — declare the start destination at the host level:
 * @Composable fun App() {
 *     NavEaseHost(start = AppScreens.Splash, onExitRequest = { finish() })
 * }
 *
 * // Typed overload — works on every platform, recommended for KMP projects:
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
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoRegister
