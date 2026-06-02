package com.alim.navease

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.alim.navease.screens.App
import io.github.alimsrepo.navease.generated.navEaseBootstrap

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    navEaseBootstrap()
    ComposeViewport {
        App()
    }
}