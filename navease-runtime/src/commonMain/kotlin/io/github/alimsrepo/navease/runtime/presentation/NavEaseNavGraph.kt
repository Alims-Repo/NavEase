@file:OptIn(ExperimentalSharedTransitionApi::class)

package io.github.alimsrepo.navease.runtime.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController

/**
 * Core navigation host composable.
 *
 * This composable:
 * 1. Creates and remembers the back stack via [rememberNavBackStack].
 * 2. Creates and remembers a [NavController] scoped to this composition.
 * 3. Provides the controller to the entire subtree via [LocalNavEaseController].
 * 4. Drives [NavDisplay] with the default slide transitions.
 * 5. Optionally wraps everything in [SharedTransitionLayout] for shared element transitions.
 *
 * **Do not call this directly** — use the KSP-generated `NavEaseHost()` composable instead.
 *
 * @param initialScreen         The first screen placed on the back stack.
 * @param savedStateConfig      Serialization config for back-stack state restoration.
 * @param screenFactory         Maps a [NavKey] to its [NavScreen]. Typically KSP-generated.
 * @param onExitRequest         Called when back is pressed at the root screen. No-op by default.
 * @param enableSharedTransitions When `true`, wraps the display in [SharedTransitionLayout] and
 *                              wires the [SharedTransitionScope] into both [NavDisplay] (native
 *                              support) and [LocalNavEaseSharedTransitionScope] (for user screens).
 *                              Defaults to `false`.
 * @param navTransition         The screen-transition animation style. Defaults to [NavTransition.Push]
 *                              (iOS-style horizontal slide). See [NavTransition] for all options.
 */
@Composable
fun NavEaseNavGraph(
    initialScreen: NavKey,
    savedStateConfig: SavedStateConfiguration,
    screenFactory: (NavKey) -> NavScreen,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) {
    val applicationStack = rememberNavBackStack(
        configuration = savedStateConfig,
        initialScreen,
    )

    // Capture onExitRequest as an updated state so that if the lambda identity changes
    // (e.g. because the caller is recomposed with a new lambda), the NavController still
    // calls the latest version without needing to be recreated.
    val currentOnExitRequest by rememberUpdatedState(onExitRequest)

    // Re-create the NavController when the app-level default transition changes so that
    // defaultTransition inside the controller always reflects the current value.
    val navController = remember(navTransition) {
        NavController(
            backStack = applicationStack,
            showExitDialog = { currentOnExitRequest() },
            defaultTransition = navTransition,
        )
    }

    // Extracts the NavDisplay call so it can be reused in both branches.
    // sharedScope is null when transitions are disabled — NavDisplay skips its shared-element
    // wiring in that case (no overhead).
    @Composable
    fun Display(sharedScope: SharedTransitionScope?) {
        CompositionLocalProvider(LocalNavEaseController provides navController) {
            NavDisplay(
                modifier = Modifier
                    .fillMaxSize(),
                backStack = applicationStack,
                sharedTransitionScope = sharedScope,
                // Look up the per-navigate transition for the destination; fall back to the
                // app-level default if the key has no recorded override (e.g. the root screen).
                // targetState is Scene<NavKey>; Scene.key == NavEntry.contentKey == navKey.toString()
                transitionSpec = {
                    val transition = navController.transitionStore[targetState.key] ?: navTransition
                    Animations.forward(transition)
                },
                // For pops, read the transition that was used to push the screen being removed
                // (initialState) so the pop plays the same animation in reverse.
                popTransitionSpec = {
                    val transition = navController.transitionStore[initialState.key] ?: navTransition
                    Animations.back(transition)
                },
            ) { route ->
                NavEntry(route) {
                    screenFactory(route).Content(
                        navKey = route,
                        navController = navController,
                    )
                }
            }
        }
    }

    if (enableSharedTransitions) {
        SharedTransitionLayout {
            // Make the SharedTransitionScope available to every screen in the subtree
            // via LocalNavEaseSharedTransitionScope.current.
            // The AnimatedVisibilityScope (AnimatedContentScope) is provided per-entry
            // by NavDisplay via LocalNavAnimatedContentScope (androidx.navigation3.ui).
            CompositionLocalProvider(LocalNavEaseSharedTransitionScope provides this) {
                Display(sharedScope = this)
            }
        }
    } else {
        Display(sharedScope = null)
    }
}
