package com.alim.navease

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.alim.navease.screens.App
import io.github.alimsrepo.navease.generated.navEaseBootstrap

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Bootstrap NavEase before the first composition.
    // Required for JS/WasmJS — Kotlin/JS IR DCE can eliminate module-level
    // initializers that aren't transitively reachable from main().
    navEaseBootstrap()
    ComposeViewport {
        App()
    }
}