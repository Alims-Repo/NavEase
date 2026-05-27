package io.github.alimsrepo.navease

import androidx.compose.runtime.Composable

/**
 * Factory interface that maps [NavEaseKey] instances to their composable screen content.
 *
 * The KSP processor generates an implementation of this interface automatically.
 * You can also implement it manually for testing or dynamic screen registration.
 */
interface NavEaseScreenFactory {

    /**
     * Renders the composable content for the given [key].
     *
     * @param key The route key identifying which screen to display.
     * @param nav The [NavEaseController] for navigation actions.
     */
    @Composable
    fun Content(key: NavEaseKey, nav: NavEaseController)

    /**
     * Returns the [Transition] animation that should be used when navigating **to** the
     * screen identified by [key].
     *
     * @param key The destination route key.
     */
    fun transitionFor(key: NavEaseKey): Transition
}

