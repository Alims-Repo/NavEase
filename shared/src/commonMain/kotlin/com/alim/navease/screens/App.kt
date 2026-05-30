package com.alim.navease.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import io.github.alimsrepo.navease.generated.autoRegisterScreens
import io.github.alimsrepo.navease.runtime.presentation.NavEaseHost
import io.github.alimsrepo.navease.runtime.presentation.NavTransition

@Composable
fun App() {
    MaterialTheme {
        NavEaseHost<AppScreens>(
            start = AppScreens.Splash,
            onExitRequest = {},
            enableSharedTransitions = true,
            navTransition = NavTransition.Push,
        ) {
            autoRegisterScreens()
        }
    }
}