/*
 * Copyright 2024 The Android Open Source Project
 * Copyright 2026 NavEase Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file is derived from AndroidX Navigation 3 and has been modified for
 * NavEase: repackaged under io.github.alimsrepo.navease.internal, and trimmed
 * to what NavEase uses.
 */
package io.github.alimsrepo.navease.internal.navigation.scene

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.alimsrepo.navease.internal.runtime.NavEntryDecorator
import io.github.alimsrepo.navease.internal.navigation.ui.LocalNavAnimatedContentScope

/** Returns a [SharedEntryInSceneNavEntryDecorator] that is remembered across recompositions. */
@Composable
internal fun <T : Any> rememberSharedEntryInSceneNavEntryDecorator(
    sharedTransitionScope: SharedTransitionScope
): SharedEntryInSceneNavEntryDecorator<T> =
    remember(sharedTransitionScope) { SharedEntryInSceneNavEntryDecorator(sharedTransitionScope) }

/**
 * A [NavEntryDecorator] that wraps each entry in a
 * [SharedTransitionScope.sharedElement] to allow nav displays to animate
 * arbitrarily place entries in different places in the composable call hierarchy.
 *
 * This should be wrapped around the [SceneSetupNavEntryDecorator].
 */
internal class SharedEntryInSceneNavEntryDecorator<T : Any>(
    sharedTransitionScope: SharedTransitionScope
) :
    NavEntryDecorator<T>(
        decorate = { entry ->
            val currentScene = LocalCurrentScene.current
            if (currentScene != null && currentScene !is OverlayScene<*>) {
                with(sharedTransitionScope) {
                    Box(
                        Modifier.sharedElement(
                            rememberSharedContentState(entry.contentKey),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                        )
                    ) {
                        entry.Content()
                    }
                }
            } else {
                entry.Content()
            }
        }
    )

internal val LocalCurrentScene: ProvidableCompositionLocal<Scene<*>?> = compositionLocalOf { null }
