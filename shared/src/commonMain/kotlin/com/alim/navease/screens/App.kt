package com.alim.navease.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
            add(SplashScreen())
            add(HomeScreen())
            add(LibraryDetailScreen())
            add(NavEaseDemoScreen())
            add(SecureVaultDemoScreen())
            add(FlowTabDemoScreen())
            add(PrayerTimesDemoScreen())
            add(CrashGuardDemoScreen())
            add(PdfDemoScreen())
            add(TransitionPreviewScreen())
        }
    }
}