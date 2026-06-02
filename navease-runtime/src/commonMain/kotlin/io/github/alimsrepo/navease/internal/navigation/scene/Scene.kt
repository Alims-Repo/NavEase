package io.github.alimsrepo.navease.internal.navigation.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import io.github.alimsrepo.navease.internal.runtime.NavEntry

/**
 * A specific scene to render 1 or more [NavEntry]s.
 *
 * A scene instance is identified by its [key] and the class of the [Scene], and this change drives
 * the top-level animation based on the [SceneStrategy] calculating what the current [Scene] is for
 * the backstack.
 *
 * The rendering for [content] should invoke the content for each
 * [NavEntry] contained in [entries] at most once concurrently in a
 * given [Scene].
 *
 * It is valid for two different instances of a [Scene] to render the same
 * [NavEntry]. In this situation, the content for a
 * [NavEntry] will only be rendered in the most recent target [Scene]
 * that it is displayed in, as determined by [entries].
 *
 * **Important** Implementations of this interface should either be data classes, or implement
 * equals and hashcode to ensure that the same [Scene] is used when appropriate.
 */
@Immutable
public interface Scene<T : Any> {
    /**
     * The key identifying the [Scene]. This key will be combined with the class of the [Scene] to
     * determine the key that drives the transition in the top-level animation for the NavDisplay.
     *
     * Because the class of the [Scene] is also used, this [key] only needs to be unique for a given
     * type of [Scene] to indicate a different instance of the [Scene].
     */
    public val key: Any

    /**
     * The list of [NavEntry]s that can be displayed in this scene.
     *
     * When animating between scenes, the underlying content for each
     * [NavEntry] will only be rendered by the scene that is most
     * recently the target scene, and is displaying that [NavEntry] as
     * determined by this [entries] list.
     *
     * For example, consider a transition from `Scene1` to `Scene2` below:
     * ```
     * Scene1:      Scene2:
     * +---+---+     +---+---+
     * |   |   |     |   |   |
     * | A | B | --> | B | C |
     * |   |   |     |   |   |
     * +---+---+     +---+---+
     * ```
     *
     * `Scene1.entries` should be `[A, B]`, and `Scene2.entries` should be `[B, C]`
     *
     * When both are being rendered at the same time during the transition, the content for `A` will
     * be rendered in `Scene1`, while the content for `B` and `C` will be rendered in `Scene2`.
     */
    public val entries: List<NavEntry<T>>

    /**
     * The resulting [NavEntry]s that should be computed after pressing back updates the backstack.
     *
     * This is required for calculating the proper predictive back state, which may result in a
     * different scene being shown.
     *
     * When predictive back is occurring, this list of entries will be passed through the
     * [SceneStrategy] again, to determine what the resulting scene would be if the back happens.
     */
    public val previousEntries: List<NavEntry<T>>

    /**
     * The content rendering the [Scene] itself.
     *
     * This should call the content for the [entries], and can add any arbitrary UI around them
     * specific to the [Scene].
     */
    public val content: @Composable () -> Unit

    /**
     * Provide [Scene]-specific information to [io.github.alimsrepo.navease.internal.navigation.ui.NavDisplay].
     *
     * By default includes the metadata of the last [NavEntry] in [entries].
     *
     * @sample androidx.navigation3.ui.samples.SceneDefaultTransitionsSample
     * @sample androidx.navigation3.ui.samples.SceneOverrideEntryTransitionsSample
     */
    public val metadata: Map<String, Any>
        get() = entries.lastOrNull()?.metadata ?: emptyMap()
}
