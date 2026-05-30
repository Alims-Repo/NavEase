package io.github.alimsrepo.navease.runtime.presentation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import io.github.alimsrepo.navease.runtime.navigation.NavController
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
 * // 1. Define your NavKey sealed class (same @Serializable sealed class as always):
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
     * Populated by [NavEaseScreenScope.add] — holds the runtime [KClass] of [K] so
     * [NavEaseHost] can dispatch the correct screen for an incoming [NavKey].
     *
     * Do **not** set this manually; always register screens via [NavEaseHost]'s
     * trailing `screens` lambda.
     */
    @PublishedApi
    internal var _keyClass: KClass<*>? = null

    /**
     * Serializer for [K], populated alongside [_keyClass] by [NavEaseScreenScope.add].
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
     * @param navController The [NavController] scoped to the current nav host.
     *                      Also available deeper in the tree via
     *                      [LocalNavEaseController.current].
     */
    @Composable
    abstract fun Content(navKey: K, navController: NavController)
}

