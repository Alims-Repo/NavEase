package io.github.alimsrepo.navease.runtime.composition

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController

/**
 * A [androidx.compose.runtime.CompositionLocal] that provides the nearest [NavEaseController]
 * to any composable in the tree without manual parameter passing.
 *
 * [NavEaseNavGraph][io.github.alimsrepo.navease.runtime.host.NavEaseNavGraph] automatically
 * provides the current [NavEaseController] via
 * [androidx.compose.runtime.CompositionLocalProvider], so all composables hosted inside it
 * can read the controller with:
 *
 * ```kotlin
 * val navController = LocalNavEaseController.current
 * ```
 *
 * The value is `null` outside of a nav-host composition (e.g. in Compose Previews).
 * Guard with a null-check or use `!!` only when you are certain a host is in scope.
 */
val LocalNavEaseController: ProvidableCompositionLocal<NavEaseController?> = compositionLocalOf { null }

