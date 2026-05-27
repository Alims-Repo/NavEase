package io.github.alimsrepo.navease.runtime.domain

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import io.github.alimsrepo.navease.runtime.navigation.NavController

/**
 * Base class for every screen in a NavEase navigation graph.
 *
 * Extend this class and annotate the subclass with
 * [@NavEaseScreen][io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen] so that
 * the KSP processor can discover it and generate the route sealed class, screen factory,
 * typed navigation/args extensions, and nav host.
 */
abstract class NavScreen {

    /**
     * Composable entry point for this screen.
     *
     * @param navKey        The key instance currently on top of the back stack for this screen.
     *                      Use the KSP-generated `navKey.xxxArgs()` extension to access typed
     *                      route arguments without referencing generated types directly.
     * @param navController The [NavController] scoped to the current navigation host.
     *                      Use [io.github.alimsrepo.navease.runtime.presentation.LocalNavEaseController]
     *                      if you need the controller from a deeply nested composable.
     */
    @Composable
    @Suppress("unused") // Called by NavEaseNavGraph via screenFactory(route).Content(route, navController)
    abstract fun Content(
        navKey: NavKey,
        navController: NavController
    )
}