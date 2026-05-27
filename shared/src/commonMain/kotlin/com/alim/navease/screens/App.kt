package com.alim.navease.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import io.github.alimsrepo.navease.generated.ScreenFactory
import io.github.alimsrepo.navease.runtime.presentation.AppNavGraph

@Composable
fun App() {
    MaterialTheme {
        AppNavGraph(screenFactory = ScreenFactory::createScreen)
    }
}