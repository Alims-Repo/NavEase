package com.alim.navease.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import io.github.alimsrepo.navease.runtime.host.NavEaseHost
import io.github.alimsrepo.navease.runtime.transition.NavTransition

@Composable
fun App() {

    MaterialTheme {
        NavEaseHost<AppScreens>(
            onExitRequest = {},
            enableSharedTransitions = true,
            navTransition = NavTransition.Push
        ) {
            add(SplashScreen(), startWith = AppScreens.Splash)
            add(HomeScreen())
            add(LibraryDetailScreen())
            add(NavEaseDemoScreen())
            add(TransitionPreviewScreen())
            add(SecureVaultDemoScreen())
            add(FlowTabDemoScreen())
            add(PrayerTimesDemoScreen())
            add(CrashGuardDemoScreen())
            add(PdfDemoScreen())
        }
    }
}