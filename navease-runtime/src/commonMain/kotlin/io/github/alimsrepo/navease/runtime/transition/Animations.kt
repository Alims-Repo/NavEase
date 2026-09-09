package io.github.alimsrepo.navease.runtime.transition

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

/**
 * Pre-computed [ContentTransform] pairs for every [NavTransition] style.
 *
 * All values are internal singletons — no allocation occurs during navigation.
 * [NavEaseHost][io.github.alimsrepo.navease.runtime.host.NavEaseHost] calls
 * [forward] / [back] with the chosen [NavTransition] to feed into `NavDisplay`'s
 * `transitionSpec` / `popTransitionSpec`.
 */
internal object Animations {

    // ── Easing ──────────────────────────────────────────────────────────────

    /** Decelerating ease-out: content arrives fast, settles gently. */
    private val EaseOutQuart = CubicBezierEasing(0.25f, 1.0f, 0.5f, 1.0f)

    /** Accelerating ease-in: content starts slow then exits swiftly. */
    private val EaseInQuart  = CubicBezierEasing(0.5f,  0.0f, 0.75f, 0.0f)

    // ── Duration constants ───────────────────────────────────────────────────

    /** Slide / scale travel duration. */
    private const val SLIDE_MS = 380

    /** Cross-fade duration — shorter so content changes read before the move ends. */
    private const val FADE_MS  = 240

    /** Extended duration for the depth / Z-axis illusion. */
    private const val DEPTH_MS = 420

    // ── Push — horizontal slide (original iOS / native-mobile feel) ─────────

    private const val PUSH_MS = 450

    private val pushForward = ContentTransform(
        targetContentEnter = slideInHorizontally(tween(PUSH_MS)) { it },
        initialContentExit = slideOutHorizontally(tween(PUSH_MS)) { -it / 4 },
        sizeTransform = SizeTransform(clip = false),
    )

    private val pushBack = ContentTransform(
        targetContentEnter = slideInHorizontally(tween(PUSH_MS)) { -it / 4 },
        initialContentExit = slideOutHorizontally(tween(PUSH_MS)) { it },
        sizeTransform = SizeTransform(clip = false),
    )

    // ── Fade — symmetric cross-dissolve ─────────────────────────────────────

    private val symmetricFade = ContentTransform(
        targetContentEnter = fadeIn(tween(SLIDE_MS, easing = EaseOutQuart)),
        initialContentExit = fadeOut(tween(SLIDE_MS, easing = EaseInQuart)),
    )

    // ── Rise — vertical slide from the bottom ───────────────────────────────

    private val riseForward = ContentTransform(
        targetContentEnter =
            fadeIn(tween(FADE_MS, easing = EaseOutQuart)) +
            slideInVertically(tween(SLIDE_MS, easing = EaseOutQuart)) { it },
        initialContentExit =
            fadeOut(tween(FADE_MS, easing = EaseInQuart)) +
            slideOutVertically(tween(SLIDE_MS, easing = EaseInQuart)) { -it / 8 },
        sizeTransform = SizeTransform(clip = false),
    )

    private val riseBack = ContentTransform(
        targetContentEnter =
            fadeIn(tween(FADE_MS, easing = EaseOutQuart)) +
            slideInVertically(tween(SLIDE_MS, easing = EaseOutQuart)) { -it / 8 },
        initialContentExit =
            fadeOut(tween(FADE_MS, easing = EaseInQuart)) +
            slideOutVertically(tween(SLIDE_MS, easing = EaseInQuart)) { it },
        sizeTransform = SizeTransform(clip = false),
    )

    // ── Zoom — scale + fade (modern drill-down feel) ────────────────────────

    private val zoomForward = ContentTransform(
        targetContentEnter =
            scaleIn(initialScale = 0.86f, animationSpec = tween(SLIDE_MS, easing = EaseOutQuart)) +
            fadeIn(tween(FADE_MS, easing = EaseOutQuart)),
        initialContentExit =
            fadeOut(tween(FADE_MS, easing = EaseInQuart)),
    )

    private val zoomBack = ContentTransform(
        targetContentEnter =
            fadeIn(tween(FADE_MS, easing = EaseOutQuart)),
        initialContentExit =
            scaleOut(targetScale = 0.86f, animationSpec = tween(SLIDE_MS, easing = EaseInQuart)) +
            fadeOut(tween(FADE_MS, easing = EaseInQuart)),
    )

    // ── Depth — Material 3 Z-axis shared-axis motion ────────────────────────

    private val depthForward = ContentTransform(
        targetContentEnter =
            scaleIn(initialScale = 0.80f, animationSpec = tween(DEPTH_MS, easing = EaseOutQuart)) +
            fadeIn(tween(DEPTH_MS, easing = EaseOutQuart)),
        initialContentExit =
            scaleOut(targetScale = 1.12f, animationSpec = tween(DEPTH_MS, easing = EaseInQuart)) +
            fadeOut(tween(DEPTH_MS, easing = EaseInQuart)),
    )

    private val depthBack = ContentTransform(
        targetContentEnter =
            scaleIn(initialScale = 1.12f, animationSpec = tween(DEPTH_MS, easing = EaseOutQuart)) +
            fadeIn(tween(DEPTH_MS, easing = EaseOutQuart)),
        initialContentExit =
            scaleOut(targetScale = 0.80f, animationSpec = tween(DEPTH_MS, easing = EaseInQuart)) +
            fadeOut(tween(DEPTH_MS, easing = EaseInQuart)),
    )

    // ── Instant — no animation ───────────────────────────────────────────────

    private val instantTransform = ContentTransform(
        targetContentEnter = EnterTransition.None,
        initialContentExit = ExitTransition.None,
    )

    // ── Public dispatch ──────────────────────────────────────────────────────

    /** Returns the [ContentTransform] used when *navigating forward* to a new screen. */
    fun forward(navTransition: NavTransition): ContentTransform = when (navTransition) {
        NavTransition.Push -> pushForward
        NavTransition.Fade -> symmetricFade
        NavTransition.Rise -> riseForward
        NavTransition.Zoom -> zoomForward
        NavTransition.Depth -> depthForward
        NavTransition.Instant -> instantTransform
    }

    /** Returns the [ContentTransform] used when *popping back* to the previous screen. */
    fun back(navTransition: NavTransition): ContentTransform = when (navTransition) {
        NavTransition.Push -> pushBack
        NavTransition.Fade -> symmetricFade
        NavTransition.Rise -> riseBack
        NavTransition.Zoom -> zoomBack
        NavTransition.Depth -> depthBack
        NavTransition.Instant -> instantTransform
    }
}

