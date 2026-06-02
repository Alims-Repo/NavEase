package io.github.alimsrepo.navease.internal.navigation.scene

import androidx.compose.runtime.Composable
import androidx.compose.ui.util.fastMap
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.NavigationEventState
import androidx.navigationevent.compose.rememberNavigationEventState

/**
 * Remembers and returns a [NavigationEventState] instance for a [SceneState].
 *
 * This composable creates and remembers a [NavigationEventState] object, which holds a
 * [NavigationEventHandler] internally. This is the state object that can be passed to
 * [NavigationBackHandler] (the composable) to "hoist" the state.
 *
 * The state's handler info (currentInfo and backInfo) is kept in sync with the provided
 * [sceneState].
 *
 * @param T the type of the key in the [SceneState].
 * @param sceneState the [SceneState] that this state will track.
 * @return a stable, remembered [NavigationEventState] instance.
 */
@Composable
public fun <T : Any> rememberNavigationEventState(
    sceneState: SceneState<T>
): NavigationEventState<SceneInfo<T>> {
    return rememberNavigationEventState(
        currentInfo = SceneInfo(sceneState.currentScene),
        backInfo = sceneState.previousScenes.fastMap { SceneInfo(it) },
    )
}
