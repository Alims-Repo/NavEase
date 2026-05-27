package io.github.alimsrepo.navease.runtime.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.alimsrepo.navease.runtime.data.Animations.popTransitionSpec
import io.github.alimsrepo.navease.runtime.data.Animations.transitionSpec
import io.github.alimsrepo.navease.runtime.data.NavController
import io.github.alimsrepo.navease.runtime.domain.NavScreen

/**
 * Core navigation host composable.
 *
 * @param initialScreen    The first screen placed on the back stack.
 * @param savedStateConfig Serialization config used by [rememberNavBackStack] for state restoration.
 * @param screenFactory    Maps a [NavKey] to its corresponding [NavScreen]. Typically the
 *                         KSP-generated `ScreenFactory::createScreen`.
 * @param onExitRequest    Called when the user attempts to navigate back from the root screen
 *                         (i.e. back stack has exactly one entry). Use this to show an exit
 *                         confirmation dialog or finish the host Activity/window.
 *                         Defaults to a no-op so platforms without an exit concept (e.g. iOS)
 *                         require no configuration.
 */
@Composable
fun AppNavGraph(
    initialScreen: NavKey,
    savedStateConfig: SavedStateConfiguration,
    screenFactory: (NavKey) -> NavScreen<*>,
    onExitRequest: () -> Unit = {}
) {
    val applicationStack = rememberNavBackStack(
        configuration = savedStateConfig, initialScreen,
    )

    val navController = remember {
        NavController(backStack = applicationStack, showExitDialog = onExitRequest)
    }

    NavDisplay(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        backStack = applicationStack,
        transitionSpec = { transitionSpec },
        popTransitionSpec = { popTransitionSpec },
    ) { route ->
        NavEntry(route) {
            // Safe cast: every NavKey in applicationStack was placed there by NavController.navigate(),
            // which only accepts keys that screenFactory can resolve to a NavScreen<T> where T == key type.
            // If this ever throws ClassCastException it means a NavKey was injected into the back stack
            // from outside NavController — which is unsupported.
            @Suppress("UNCHECKED_CAST")
            (screenFactory(route) as NavScreen<NavKey>).Content(
                navKey = route,
                navController = navController
            )
        }
    }
}