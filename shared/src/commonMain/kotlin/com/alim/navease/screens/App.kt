package com.alim.navease.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import io.github.alimsrepo.navease.runtime.presentation.NavEaseHost
import io.github.alimsrepo.navease.runtime.presentation.NavTransition

@Composable
fun App() {
    MaterialTheme {
        // Zero-config: NavEaseHost() auto-discovers all @AutoRegister screens.
        // On Android/JVM screens are found automatically via class loading.
        // On iOS, navEaseBootstrap() is called from MainViewController.kt.
        NavEaseHost(
            onExitRequest = {},
            enableSharedTransitions = true,
            navTransition = NavTransition.Push
        )
    }
}