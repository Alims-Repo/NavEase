package com.alim.navease.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.alimsrepo.navease.generated.navEaseBootstrap
import io.github.alimsrepo.navease.runtime.presentation.NavEaseHost
import io.github.alimsrepo.navease.runtime.presentation.NavTransition

@Composable
fun App() {
    // Triggers the KSP-generated @AutoRegister initializer on iOS/Native/JS.
    // On Android/JVM this is a no-op — screens are already discovered via class loading.
    // Called once per composition via remember; safe and cheap on all platforms.
    remember { navEaseBootstrap() }

    MaterialTheme {
        NavEaseHost(
            onExitRequest = {},
            enableSharedTransitions = true,
            navTransition = NavTransition.Push
        )
    }
}