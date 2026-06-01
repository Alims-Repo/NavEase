package com.alim.navease

import androidx.compose.ui.window.ComposeUIViewController
import com.alim.navease.screens.App
import io.github.alimsrepo.navease.generated.navEaseBootstrap

/**
 * iOS entry point for the NavEase sample app.
 *
 * [App] internally calls `navEaseBootstrap()` via `remember`, so no explicit
 * bootstrap call is needed here. The platform entry point stays clean and
 * framework-agnostic.
 */
fun MainViewController() = ComposeUIViewController {
    navEaseBootstrap()
    App()
}
