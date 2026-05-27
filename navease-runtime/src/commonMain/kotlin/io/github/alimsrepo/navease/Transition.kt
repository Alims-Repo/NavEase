package io.github.alimsrepo.navease

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

/**
 * Built-in screen transition animations for NavEase.
 *
 * Pass one of these values to the [NavEaseScreen.transition] parameter to control
 * how a screen enters and exits during navigation.
 *
 * You can also supply any [androidx.compose.animation.ContentTransform] directly via
 * [NavEaseHost] for fully custom animations.
 */
enum class Transition {
    /** Horizontal slide transition — new screen slides in from the right (default, iOS-style). */
    SLIDE,

    /** Cross-fade transition — screens fade in/out. */
    FADE,

    /** No animation. Screens switch instantly. */
    NONE,
}

/**
 * Converts this [Transition] to a Compose [ContentTransform].
 *
 * @param reverse When `true` the direction is reversed (used for back navigation).
 */
internal fun Transition.toContentTransform(reverse: Boolean = false): ContentTransform =
    when (this) {
        Transition.SLIDE -> if (reverse) {
            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
        } else {
            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
        }

        Transition.FADE -> fadeIn() togetherWith fadeOut()

        Transition.NONE -> EnterTransition.None togetherWith ExitTransition.None
    }

