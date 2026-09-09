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

import androidx.compose.runtime.Immutable
import androidx.navigationevent.NavigationEventHistory
import androidx.navigationevent.NavigationEventInfo

/**
 * Represents a snapshot of the visible destinations in a navigation container.
 *
 * This class provides the necessary context for building animations during navigation gestures,
 * like predictive back. It's a simple data holder that feeds into the [NavigationEventHistory].
 *
 * @property scene The scene whose state is used by the NavigationEvent
 */
@Immutable
public class SceneInfo<T : Any>(public val scene: Scene<T>) : NavigationEventInfo() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SceneInfo<*>

        return scene == other.scene
    }

    override fun hashCode(): Int {
        return scene.hashCode()
    }

    override fun toString(): String {
        return "SceneInfo(scene=$scene)"
    }
}
