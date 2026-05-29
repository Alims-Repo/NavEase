package io.github.alimsrepo.navease.runtime.presentation

/**
 * Selects the screen-transition animation style used by [NavEaseNavGraph] / `NavEaseHost`.
 *
 * Pass as the `navTransition` parameter of `NavEaseHost`:
 *
 * ```kotlin
 * NavEaseHost(navTransition = NavTransition.Rise)
 * ```
 *
 * All options include carefully tuned easing so every swap looks polished
 * regardless of which style you choose.
 */
sealed class NavTransition {

    /**
     * **Push** — horizontal slide, iOS / native-mobile feel.
     *
     * Forward: destination glides in from the right while the source nudges slightly
     * left (parallax effect). Back: the reverse.
     *
     * _This is the **default**._
     */
    data object Push : NavTransition()

    /**
     * **Fade** — symmetric cross-fade.
     *
     * Outgoing screen dissolves out while the incoming screen dissolves in.
     * Works well for screens that do not share a natural spatial relationship.
     */
    data object Fade : NavTransition()

    /**
     * **Rise** — vertical slide from the bottom up.
     *
     * Destination rises from below (think bottom-sheets or "next step" flows).
     * Back: destination drops back down.
     */
    data object Rise : NavTransition()

    /**
     * **Zoom** — scale-and-fade.
     *
     * Destination expands from ~86 % while fading in; source fades out without
     * scaling. Back is the mirror: source shrinks away while destination fades in.
     * Gives a modern "drill-down" feel.
     */
    data object Zoom : NavTransition()

    /**
     * **Depth** — Material Design 3 Z-axis shared-axis motion.
     *
     * Forward: destination scales up from 80 % (content "surfaces towards you")
     * while source scales past 100 % and fades (recedes). Back: the reverse.
     * Creates a convincing depth illusion.
     */
    data object Depth : NavTransition()

    /**
     * **Instant** — no animation whatsoever.
     *
     * Screens swap immediately with zero transition.
     * Useful for testing or deeply nested flows where animation would feel redundant.
     */
    data object Instant : NavTransition()
}

