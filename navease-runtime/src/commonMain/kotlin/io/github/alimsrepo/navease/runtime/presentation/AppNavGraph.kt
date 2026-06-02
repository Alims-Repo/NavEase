package io.github.alimsrepo.navease.runtime.presentation

import androidx.compose.runtime.Composable
import io.github.alimsrepo.navease.internal.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.transition.NavTransition

// ---------------------------------------------------------------------------
// DEPRECATED — AppNavGraph has been renamed to NavEaseNavGraph and moved to
//              io.github.alimsrepo.navease.runtime.host.
// ---------------------------------------------------------------------------

/** @suppress */
@Deprecated(
    message = "AppNavGraph has been renamed to NavEaseNavGraph and moved to " +
              "io.github.alimsrepo.navease.runtime.host. " +
              "If this call is in KSP-generated code, re-run kspCommonMainKotlinMetadata.",
    replaceWith = ReplaceWith(
        "NavEaseNavGraph(initialScreen, savedStateConfig, screenFactory, onExitRequest, enableSharedTransitions, navTransition)",
        "io.github.alimsrepo.navease.runtime.host.NavEaseNavGraph"
    ),
    level = DeprecationLevel.WARNING
)
@Composable
fun AppNavGraph(
    initialScreen: NavKey,
    savedStateConfig: SavedStateConfiguration,
    screenFactory: (NavKey) -> NavScreen,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) = io.github.alimsrepo.navease.runtime.host.NavEaseNavGraph(
    initialScreen = initialScreen,
    savedStateConfig = savedStateConfig,
    screenFactory = screenFactory,
    onExitRequest = onExitRequest,
    enableSharedTransitions = enableSharedTransitions,
    navTransition = navTransition,
)

