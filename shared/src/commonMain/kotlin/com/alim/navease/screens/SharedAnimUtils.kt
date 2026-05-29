@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.alim.navease.screens

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

// ── BoundsTransform presets ────────────────────────────────────────────────────
//
// BoundsTransform is defined as top-level vals so the *same instance* is reused
// on both the source and the destination end of every shared transition.
// This avoids allocating a new lambda on every recomposition and keeps
// Compose's equality checks stable.
//
// dampingRatio guide:
//   1.0  = critically damped (no overshoot, just smooth deceleration)
//   0.75 = low bounce  (subtle, tasteful overshoot — iOS-style "settle")
//   0.5  = medium bounce
//
// stiffness guide:
//   1500 = Spring.StiffnessMedium   — snaps quickly (~300 ms to settle)
//    700 = between Medium/MediumLow — takes ~400 ms, feels more considered
//    550 = closer to StiffnessMediumLow — ~500 ms, luxurious for expansions

/**
 * **Card morph** — used when a Feature card in MainScreen morphs into the
 * Detail screen header.  Stiffness is high enough to feel snappy; the slight
 * underdamp gives a polished, premium "settle".
 */
val CardMorphBoundsTransform = BoundsTransform { _, _ ->
    spring(dampingRatio = 0.82f, stiffness = 700f)
}

/**
 * **Avatar fly** — the small avatar circle in MainScreen flies to the large
 * profile hero in ProfileScreen.  Slightly more springy than the card morph so
 * the movement feels playful and alive.
 */
val AvatarBoundsTransform = BoundsTransform { _, _ ->
    spring(dampingRatio = 0.74f, stiffness = 600f)
}

/**
 * **Gallery expand** — a compact list-row card expands into a full hero in
 * GalleryDetailScreen.  This is the most dramatic size change so it gets the
 * softest spring — the expansion "breathes" open.
 */
val GalleryHeroBoundsTransform = BoundsTransform { _, _ ->
    spring(dampingRatio = 0.68f, stiffness = 550f)
}

// ── Shared fade specs ──────────────────────────────────────────────────────────
//
// During a sharedBounds transition the *content* inside the morphing container
// cross-fades.  A fast exit + slower enter (asymmetric timing) makes the source
// content "get out of the way" quickly while the destination fades in naturally.

/** Content fade-in as the destination bounds settle. */
val SharedEnterFade = fadeIn(tween(durationMillis = 380, easing = FastOutSlowInEasing))

/** Content fade-out as the source bounds begin to morph. */
val SharedExitFade  = fadeOut(tween(durationMillis = 220, easing = FastOutSlowInEasing))

/** Slightly longer fade-in for dramatic expansions (gallery hero). */
val GallerySharedEnterFade = fadeIn(tween(durationMillis = 420, easing = FastOutSlowInEasing))

