package io.github.alimsrepo.navease.runtime.presentation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.alimsrepo.navease.runtime.domain.NavScreen

// ---------------------------------------------------------------------------
// DEPRECATED — AppNavGraph has been renamed to NavEaseNavGraph.
// ---------------------------------------------------------------------------

/** @suppress */
@Deprecated(
    message = "AppNavGraph has been renamed to NavEaseNavGraph. " +
              "If this call is in KSP-generated code, re-run kspCommonMainKotlinMetadata.",
    replaceWith = ReplaceWith(
        "NavEaseNavGraph(initialScreen, savedStateConfig, screenFactory, onExitRequest, enableSharedTransitions)",
        "io.github.alimsrepo.navease.runtime.presentation.NavEaseNavGraph"
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
) = NavEaseNavGraph(
    initialScreen = initialScreen,
    savedStateConfig = savedStateConfig,
    screenFactory = screenFactory,
    onExitRequest = onExitRequest,
    enableSharedTransitions = enableSharedTransitions,
)

