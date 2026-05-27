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

@Suppress("UNCHECKED_CAST")
@Composable
fun AppNavGraph(
    initialScreen: NavKey,
    savedStateConfig: SavedStateConfiguration,
    screenFactory: (NavKey) -> NavScreen<*>
) {
    val applicationStack = rememberNavBackStack(
        configuration = savedStateConfig, initialScreen,
    )

    val navController = remember {
        NavController(backStack = applicationStack, showExitDialog = { })
    }

    NavDisplay(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        backStack = applicationStack,
        transitionSpec = { transitionSpec },
        popTransitionSpec = { popTransitionSpec },
    ) { route ->
        NavEntry(route) {
            (screenFactory(route) as NavScreen<NavKey>).Content(
                navKey = route,
                navController = navController
            )
        }
    }
}