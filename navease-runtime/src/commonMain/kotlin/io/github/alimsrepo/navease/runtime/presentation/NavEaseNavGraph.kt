package io.github.alimsrepo.navease.runtime.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
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
 * 4. Drives [androidx.navigation3.ui.NavDisplay] with the default slide transitions.
 *
 * **Do not call this directly** — use the KSP-generated `NavEaseHost()` composable instead.
 * It wires [initialScreen], [savedStateConfig], and [screenFactory] automatically.
 *
 * @param initialScreen    The first screen placed on the back stack.
 * @param savedStateConfig Serialization config used by [rememberNavBackStack] for process-death
 *                         state restoration.
 * @param screenFactory    Maps a [NavKey] to its corresponding [NavScreen]. Typically the
 *                         KSP-generated `ScreenFactory::createScreen`.
 * @param onExitRequest    Called when the user attempts to navigate back from the root screen
 *                         (i.e. the back stack has exactly one entry). Use this to show an exit
 *                         confirmation dialog or finish the host Activity/window.
 *                         Defaults to a no-op — suitable for iOS / web targets.
 */
@Composable
fun NavEaseNavGraph(
    initialScreen: NavKey,
    savedStateConfig: SavedStateConfiguration,
    screenFactory: (NavKey) -> NavScreen,
    onExitRequest: () -> Unit = {}
) {
    val applicationStack = rememberNavBackStack(
        configuration = savedStateConfig, initialScreen,
    )

    val navController = remember {
        NavController(backStack = applicationStack, showExitDialog = onExitRequest)
    }

    CompositionLocalProvider(LocalNavEaseController provides navController) {
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
            backStack = applicationStack,
            transitionSpec = { Animations.transitionSpec },
            popTransitionSpec = { Animations.popTransitionSpec },
        ) { route ->
            NavEntry(route) {
                screenFactory(route).Content(
                    navKey = route,
                    navController = navController
                )
            }
        }
    }
}



