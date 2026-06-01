@file:Suppress("UNCHECKED_CAST")

package io.github.alimsrepo.navease.runtime.graph

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlin.reflect.KClass

/**
 * A compiled navigation graph ready to be passed to [NavEaseNavGraph][io.github.alimsrepo.navease.runtime.host.NavEaseNavGraph]
 * or [NavEaseHost][io.github.alimsrepo.navease.runtime.host.NavEaseHost].
 *
 * Construct via [navEaseGraph] — do not instantiate directly.
 */
class NavEaseGraph @PublishedApi internal constructor(
    /** The first screen placed on the back stack when the host first composes. */
    val start: NavKey,
    internal val savedStateConfig: SavedStateConfiguration,
    private val factory: Map<KClass<*>, NavEaseEntry<*>>,
) {
    /**
     * Returns a zero-argument composable that renders [key]'s registered screen content,
     * or `null` if no screen was registered for [key]'s type.
     */
    internal fun contentFor(key: NavKey): (@Composable () -> Unit)? {
        val entry = factory[key::class] as? NavEaseEntry<NavKey> ?: return null
        return { entry.content(key) }
    }
}

