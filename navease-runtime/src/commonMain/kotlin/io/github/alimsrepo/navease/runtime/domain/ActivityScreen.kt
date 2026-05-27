package io.github.alimsrepo.navease.runtime.domain

import androidx.compose.runtime.Composable
import io.github.alimsrepo.navease.runtime.data.NavController

abstract class NavScreen<T> {

    @Composable
    abstract fun InitView(
        navKey: T,
        navController: NavController
    )
}