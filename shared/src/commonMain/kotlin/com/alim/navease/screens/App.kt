package com.alim.navease.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import io.github.alimsrepo.navease.runtime.presentation.NavEaseHost
import io.github.alimsrepo.navease.runtime.presentation.NavTransition
import io.github.alimsrepo.navease.runtime.presentation.autoRegisterScreens

@Composable
fun App() {
    MaterialTheme {
        // NavEaseHost<AppScreens> is always in the runtime library — no error before KSP.
        // autoRegisterScreens() has a no-op stub in the runtime; after KSP the typed
        // generated extension takes over, registering all @AutoRegister screens.
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