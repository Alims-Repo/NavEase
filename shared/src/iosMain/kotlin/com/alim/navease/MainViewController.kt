package com.alim.navease

import androidx.compose.ui.window.ComposeUIViewController
import com.alim.navease.screens.App

/**
 * iOS entry point for the NavEase sample app.
 *
 * The navEaseBootstrap() call initializes the @AutoRegister screen registry.
 * This is required on iOS because automatic initialization is not reliable.
 */
fun MainViewController() = ComposeUIViewController {
    App()
}
