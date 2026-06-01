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

/**
 * Provides access to the **parent** [NavEaseController] when inside a nested [NavEaseHost].
 *
 * In a nested navigation setup where one [NavEaseHost] is composed inside a screen of another,
 * the inner host overrides [LocalNavEaseController] with its own controller. Use this local
 * to reach the outer (parent) controller — for example, to navigate back in the parent flow
 * from within a nested sub-flow:
 *
 * ```kotlin
 * // Inside a nested screen:
 * val parentNav = LocalNavEaseParentController.current
 * parentNav?.navigate(AppScreens.Home)  // navigates in the outer host
 * ```
 *
 * `null` if there is no parent host (i.e. the current host is the top-level one).
 */
val LocalNavEaseParentController: ProvidableCompositionLocal<NavEaseController?> = compositionLocalOf { null }

