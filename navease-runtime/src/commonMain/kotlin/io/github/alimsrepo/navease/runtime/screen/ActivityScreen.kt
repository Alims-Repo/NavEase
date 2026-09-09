package io.github.alimsrepo.navease.runtime.screen

import androidx.compose.runtime.Composable
import io.github.alimsrepo.navease.internal.runtime.NavKey
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController

/**
 * Base class for a NavEase screen.
 *
 * [K] is the *specific* key this screen handles, so [Content] receives it already
 * typed — no casting and no argument-extraction helpers.
 *
 * ```kotlin
 * sealed class AppScreens : NavEaseRoot {
 *     data object Home : AppScreens()
 *     data class Detail(val id: String) : AppScreens()
 * }
 *
 * @AutoRegister
 * class DetailScreen : ActivityScreen<AppScreens.Detail>() {
 *     @Composable
 *     override fun Content(navKey: AppScreens.Detail, navEaseController: NavEaseController) {
 *         Text(navKey.id)   // typed
 *     }
 * }
 * ```
 *
 * Subclasses must extend `ActivityScreen<K>` **directly** and expose a no-argument
 * constructor: KSP reads [K] from the direct supertype and generates `ScreenClass()`.
 * Put shared behaviour in a composable you call from [Content], not in an intermediate
 * base class.
 *
 * A fresh instance is created per host, so a screen may hold composition-scoped state
 * without leaking it into another host.
 *
 * @param K The key type this screen handles, e.g. `AppScreens.Detail`.
 */
public abstract class ActivityScreen<K : NavKey> {

    /**
     * Renders this screen.
     *
     * @param navKey            The typed key currently on top of the back stack.
     * @param navEaseController The controller for the host that owns this screen. Also
     *                          reachable deeper in the tree via
     *                          [LocalNavEaseController][io.github.alimsrepo.navease.runtime.composition.LocalNavEaseController].
     */
    @Composable
    public abstract fun Content(navKey: K, navEaseController: NavEaseController)
}
