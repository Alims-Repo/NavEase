package com.alim.navease.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import io.github.alimsrepo.navease.runtime.host.NavEaseHost
import io.github.alimsrepo.navease.runtime.transition.NavTransition

@Composable
fun App() {

    MaterialTheme {
        NavEaseHost(
            onExitRequest = {},
            enableSharedTransitions = true,
            navTransition = NavTransition.Push
        )
    }
}