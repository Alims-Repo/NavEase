package io.github.alimsrepo.navease.runtime.presentation

import androidx.compose.runtime.Composable

/**
 * Zero-rebuild navigation host — the recommended entry point for all new NavEase projects.
 *
 * Accepts a [NavEaseGraph] built with [navEaseGraph]. No code generation, no KSP, no rebuild
 * required after adding or changing a screen.
 *
 * ## Typical usage
 *
 * ```kotlin
 * // 1. Define your NavKeys — @Serializable sealed class, no annotations needed:
 * @Serializable sealed class AppScreen : NavKey {
 *     @Serializable data object Home : AppScreen()
 *     @Serializable data class Detail(val id: String) : AppScreen()
 *     @Serializable data class Profile(val userId: Int) : AppScreen()
 * }
 *
 * // 2. Build the graph once — top-level val, or inside remember():
 * val appGraph = navEaseGraph(start = AppScreen.Home) {
 *     screen<AppScreen.Home>    { HomeScreen() }
 *     screen<AppScreen.Detail>  { key -> DetailScreen(id = key.id) }
 *     screen<AppScreen.Profile> { key -> ProfileScreen(userId = key.userId) }
 * }
 *
 * // 3. Wire the host — typically in your root composable:
 * @Composable
 * fun App() {
 *     NavEaseHost(appGraph)
 * }
 *
 * // 4. Navigate — immediately available in the IDE, zero rebuild:
 * val nav = LocalNavEaseController.current
 * nav.navigate(AppScreen.Detail(id = "abc"))
 * nav.navigate(AppScreen.Profile(userId = 42))
 * nav.pop()
 * ```
 *
 * @param graph                   The navigation graph built via [navEaseGraph].
 * @param onExitRequest           Called when the user presses back on the root screen.
 *                                Use this to show an exit dialog or finish the Activity.
 *                                Defaults to a no-op (suitable for iOS / Web targets).
 * @param enableSharedTransitions When `true`, wraps the display in a [SharedTransitionLayout]
 *                                enabling shared-element transitions between screens.
 *                                Access the scope via [LocalNavEaseSharedTransitionScope.current].
 *                                Defaults to `false`.
 * @param navTransition           The default screen-to-screen animation style.
 *                                Defaults to [NavTransition.Push] (iOS-style horizontal slide).
 *                                Override per-navigate call via `NavController.navigate(..., navTransition = ...)`.
 */
@Composable
fun NavEaseHost(
    graph: NavEaseGraph,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) {
    NavEaseNavGraph(
        graph = graph,
        onExitRequest = onExitRequest,
        enableSharedTransitions = enableSharedTransitions,
        navTransition = navTransition,
    )
}

