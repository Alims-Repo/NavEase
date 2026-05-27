package io.github.alimsrepo.navease.runtime.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.alimsrepo.navease.runtime.data.Animations.popTransitionSpec
import io.github.alimsrepo.navease.runtime.data.Animations.transitionSpec
import io.github.alimsrepo.navease.runtime.data.NavController
import io.github.alimsrepo.navease.runtime.domain.AppScreens
import io.github.alimsrepo.navease.runtime.domain.AppScreens.Companion.savedStateConfig
import io.github.alimsrepo.navease.runtime.domain.NavScreen

@Composable
fun AppNavGraph(
    screenFactory: (AppScreens) -> NavScreen<AppScreens>
) {

    val applicationStack = rememberNavBackStack(
        configuration = savedStateConfig, AppScreens.Splash
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
            screenFactory(route as AppScreens).Content(
                navKey = route,
                navController = navController
            )
        }
    }
}