package io.github.alimsrepo.navease.runtime.domain

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import io.github.alimsrepo.navease.runtime.data.NavController

@OptIn(ExperimentalSharedTransitionApi::class)
abstract class NavScreen<T> {

    @Composable
    abstract fun Content(
        navKey: T,
        navController: NavController
    )
}