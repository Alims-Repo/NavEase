package io.github.alimsrepo.navease.runtime.screen

import androidx.compose.runtime.Composable
import io.github.alimsrepo.navease.internal.runtime.NavKey
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

/**
 * Typed base class for NavEase screens — the recommended way to define screens
 * without annotations or code generation.
 *
 * Extend this class and override [Content]. The generic parameter [K] is the
 * **specific** [NavKey] subclass this screen handles, so [Content] receives a fully-typed
 * [navKey] — no casting, no `xxxArgs()` extension needed.
 *
 * ## Usage
 *
 * ```kotlin
 * // 1. Define your NavKey sealed class:
 * @Serializable sealed class AppScreens : NavKey {
 *     @Serializable data object Home   : AppScreens()
 *     @Serializable data object About  : AppScreens()
 *     @Serializable data class Detail(val id: String) : AppScreens()
 * }
 *
 * // 2. Extend ActivityScreen for each screen — navKey is already the right type:
 * class HomeScreen : ActivityScreen<AppScreens.Home>() {
 *     @Composable
 *     override fun Content(navKey: AppScreens.Home, navController: NavController) {
 *         Button(onClick = { navController.navigate(AppScreens.Detail(id = "abc")) }) {
 *             Text("Open Detail")
 *         }
 *     }
 * }
 *
 * class DetailScreen : ActivityScreen<AppScreens.Detail>() {
 *     @Composable
 *     override fun Content(navKey: AppScreens.Detail, navController: NavController) {
 *         Text(navKey.id)   // ← typed! no casting
 *     }
 * }
 *
 * // 3. Host — register screens inline, no graph DSL required:
 * @Composable fun App() {
 *     NavEaseHost<AppScreens>(start = AppScreens.Home) {
 *         add(HomeScreen())
 *         add(AboutScreen())
 *         add(DetailScreen())
 *     }
 * }
 * ```
 *
 * @param K The [NavKey] subclass this screen handles (e.g. `AppScreens.Detail`).
 *          Must be annotated with `@Serializable`.
 */
abstract class ActivityScreen<K : NavKey> {

    /**
     * Populated by [NavEaseScreenScope.add][io.github.alimsrepo.navease.runtime.graph.NavEaseScreenScope.add]
     * — holds the runtime [KClass] of [K] so the host can dispatch the correct screen.
     *
     * Do **not** set this manually; always register screens via [NavEaseHost][io.github.alimsrepo.navease.runtime.host.NavEaseHost]'s
     * trailing `screens` lambda.
     */
    @PublishedApi
    internal var _keyClass: KClass<*>? = null

    /**
     * Serializer for [K], populated alongside [_keyClass].
     * Used to build [androidx.savedstate.serialization.SavedStateConfiguration] automatically
     * so the back stack survives process death.
     */
    @PublishedApi
    internal var _serializer: KSerializer<*>? = null

    /**
     * Composable entry point for this screen.
     *
     * @param navKey        The typed nav-key currently on top of the back stack.
     *                      Route arguments are accessible directly — e.g. `navKey.id`.
     * @param navEaseController The [NavEaseController] scoped to the current nav host.
     *                      Also available deeper in the tree via
     *                      [LocalNavEaseController.current][io.github.alimsrepo.navease.runtime.composition.LocalNavEaseController].
     */
    @Composable
    abstract fun Content(navKey: K, navEaseController: NavEaseController)
}

