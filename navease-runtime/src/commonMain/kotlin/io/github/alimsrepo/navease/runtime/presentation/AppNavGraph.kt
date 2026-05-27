package io.github.alimsrepo.navease.runtime.presentation


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.alimsrepo.navease.runtime.data.Animations.popTransitionSpec
import io.github.alimsrepo.navease.runtime.data.Animations.transitionSpec
import io.github.alimsrepo.navease.runtime.data.NavController
import io.github.alimsrepo.navease.runtime.data.ScreenFactory
import io.github.alimsrepo.navease.runtime.domain.AppScreens
import io.github.alimsrepo.navease.runtime.domain.AppScreens.Companion.savedStateConfig


@Composable
fun AppNavGraph() {

    val applicationStack = rememberNavBackStack(
        configuration = savedStateConfig, AppScreens.Splash
    )

    val navController = remember {
        NavController(backStack = applicationStack, showExitDialog = { })
    }

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = applicationStack,
        transitionSpec = { transitionSpec },
        popTransitionSpec = { popTransitionSpec },
    ) { route ->
        NavEntry(route) {
            ScreenFactory.createScreen(route as AppScreens).InitView(
                navKey = route,
                navController = navController
            )
        }
    }
}