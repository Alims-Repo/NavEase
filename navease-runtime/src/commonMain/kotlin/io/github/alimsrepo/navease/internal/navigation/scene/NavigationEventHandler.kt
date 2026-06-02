package io.github.alimsrepo.navease.internal.navigation.scene

import androidx.compose.runtime.Composable
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.NavigationEventState

/**
 * A composable that handles back navigation gestures for a [SceneState], driven by a
 * [NavigationEventState].
 *
 * This is a convenience wrapper around the core
 * [androidx.navigationevent.compose.NavigationBackHandler] that automatically handles the
 * [NavigationEventState] based on the provided [sceneState].
 *
 * @param sceneState the [SceneState] that this handler is associated with.
 * @param state the hoisted [NavigationEventState] (returned from [rememberNavigationEventState]) to
 *   be registered.
 * @param onBackCancelled called if a back navigation gesture is cancelled.
 * @param onBackCompleted called when a back navigation gesture completes and navigation occurs.
 */
@Composable
public fun <T : Any> NavigationBackHandler(
    sceneState: SceneState<T>,
    state: NavigationEventState<SceneInfo<T>> = rememberNavigationEventState(sceneState),
    onBackCancelled: () -> Unit = {},
    onBackCompleted: () -> Unit,
) {
    NavigationBackHandler(
        state = state,
        isBackEnabled = sceneState.currentScene.previousEntries.isNotEmpty(),
        onBackCancelled = onBackCancelled,
        onBackCompleted = {
            // If 'enabled' becomes stale (e.g., it was set to false but a gesture was
            // dispatched in the same frame), this may result in no entries being popped
            // due to 'entries.size' being smaller than 'scene.previousEntries.size'
            // but that's preferable to crashing with an 'IndexOutOfBoundsException'
            repeat(sceneState.entries.size - sceneState.currentScene.previousEntries.size) {
                onBackCompleted()
            }
        },
    )
}
