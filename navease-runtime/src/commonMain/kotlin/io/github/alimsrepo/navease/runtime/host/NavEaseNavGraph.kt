@file:OptIn(ExperimentalSharedTransitionApi::class)

package io.github.alimsrepo.navease.runtime.host

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.alimsrepo.navease.runtime.composition.LocalNavEaseController
import io.github.alimsrepo.navease.runtime.composition.LocalNavEaseParentController
import io.github.alimsrepo.navease.runtime.composition.LocalNavEaseSharedTransitionScope
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController
import io.github.alimsrepo.navease.runtime.transition.Animations
import io.github.alimsrepo.navease.runtime.transition.NavTransition

// ─────────────────────────────────────────────────────────────────────────────
// Internal core — shared by all public overloads
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Internal engine. All public [NavEaseNavGraph] overloads delegate here so all
 * animation, controller, and shared-transition logic lives in exactly one place.
 *
 * @param contentProvider Called inside each [NavEntry] to render the current route.
 *                        [LocalNavEaseController] is already provided at that point.
 */
@Composable
internal fun NavEaseNavGraphCore(
    initialScreen: NavKey,
    savedStateConfig: SavedStateConfiguration,
    contentProvider: @Composable (NavKey) -> Unit,
    onExitRequest: () -> Unit,
    enableSharedTransitions: Boolean,
    navTransition: NavTransition,
) {
    val applicationStack = rememberNavBackStack(
        configuration = savedStateConfig,
        initialScreen,
    )

    val currentOnExitRequest by rememberUpdatedState(onExitRequest)

    val navEaseController = remember(applicationStack) {
        NavEaseController(
            backStack = applicationStack,
            showExitDialog = { currentOnExitRequest() },
            defaultTransition = navTransition,
        )
    }

    SideEffect {
        navEaseController.defaultTransition = navTransition
    }

    @Composable
    fun Display(sharedScope: SharedTransitionScope?) {
        CompositionLocalProvider(
            LocalNavEaseParentController provides LocalNavEaseController.current,
            LocalNavEaseController provides navEaseController,
        ) {
            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                backStack = applicationStack,
                sharedTransitionScope = sharedScope,
                transitionSpec = {
                    val transition = navEaseController.transitionStore[targetState.key] ?: navTransition
                    Animations.forward(transition)
                },
                popTransitionSpec = {
                    val transition = navEaseController.transitionStore[initialState.key] ?: navTransition
                    Animations.back(transition)
                },
            ) { route ->
                NavEntry(route) {
                    contentProvider(route)
                }
            }
        }
    }

    if (enableSharedTransitions) {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalNavEaseSharedTransitionScope provides this) {
                Display(sharedScope = this)
            }
        }
    } else {
        Display(sharedScope = null)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Public API — KSP / legacy variant
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Core navigation host composable — **KSP / legacy variant**.
 *
 * Accepts the classic `(NavKey) -> NavScreen` factory produced by KSP or written by hand.
 * Prefer the [NavEaseGraph][io.github.alimsrepo.navease.runtime.graph.NavEaseGraph] overload
 * for new projects — it requires no code generation and has zero rebuild friction.
 *
 * @param initialScreen           The first screen placed on the back stack.
 * @param savedStateConfig        Serialization config for back-stack state restoration.
 * @param screenFactory           Maps a [NavKey] to its [NavScreen]. Typically KSP-generated.
 * @param onExitRequest           Called when back is pressed at the root screen. No-op by default.
 * @param enableSharedTransitions When `true`, wraps in [SharedTransitionLayout].
 * @param navTransition           Default screen-transition animation. Defaults to [NavTransition.Push].
 */
@Composable
fun NavEaseNavGraph(
    initialScreen: NavKey,
    savedStateConfig: SavedStateConfiguration,
    screenFactory: (NavKey) -> NavScreen,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) = NavEaseNavGraphCore(
    initialScreen = initialScreen,
    savedStateConfig = savedStateConfig,
    contentProvider = { key ->
        val nav = LocalNavEaseController.current
            ?: error("NavEase: LocalNavEaseController is null — Content called outside NavEaseNavGraph.")
        screenFactory(key).Content(navKey = key, navEaseController = nav)
    },
    onExitRequest = onExitRequest,
    enableSharedTransitions = enableSharedTransitions,
    navTransition = navTransition,
)

// ─────────────────────────────────────────────────────────────────────────────
// Public API — zero-rebuild DSL variant
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Core navigation host composable — **zero-rebuild DSL variant**.
 *
 * Accepts a [NavEaseGraph][io.github.alimsrepo.navease.runtime.graph.NavEaseGraph] built with
 * [navEaseGraph][io.github.alimsrepo.navease.runtime.graph.navEaseGraph]. No code generation,
 * no KSP, no rebuild required after adding a new screen.
 *
 * ```kotlin
 * val appGraph = navEaseGraph(start = AppScreen.Home) {
 *     screen<AppScreen.Home> { HomeScreen() }
 *     screen<AppScreen.Detail> { key -> DetailScreen(id = key.id) }
 * }
 *
 * // In your root composable:
 * NavEaseNavGraph(appGraph)
 * ```
 *
 * @param graph                   The navigation graph built via [navEaseGraph][io.github.alimsrepo.navease.runtime.graph.navEaseGraph].
 * @param onExitRequest           Called when back is pressed at the root screen. No-op by default.
 * @param enableSharedTransitions When `true`, wraps in [SharedTransitionLayout].
 * @param navTransition           Default screen-transition animation. Defaults to [NavTransition.Push].
 */
@Composable
fun NavEaseNavGraph(
    graph: io.github.alimsrepo.navease.runtime.graph.NavEaseGraph,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) = NavEaseNavGraphCore(
    initialScreen = graph.start,
    savedStateConfig = graph.savedStateConfig,
    contentProvider = { key ->
        graph.contentFor(key)?.invoke()
            ?: error(
                "NavEase: No screen registered for '${key::class.simpleName}'. " +
                    "Did you forget to add screen<${key::class.simpleName}> { } " +
                    "inside your navEaseGraph { } block?"
            )
    },
    onExitRequest = onExitRequest,
    enableSharedTransitions = enableSharedTransitions,
    navTransition = navTransition,
)

