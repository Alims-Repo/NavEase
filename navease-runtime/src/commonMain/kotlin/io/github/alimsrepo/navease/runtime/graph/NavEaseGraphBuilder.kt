@file:Suppress("UNCHECKED_CAST")

package io.github.alimsrepo.navease.runtime.graph

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * DSL builder for [NavEaseGraph].
 *
 * Use [navEaseGraph] to obtain an instance and register screens via [screen].
 */
class NavEaseGraphBuilder @PublishedApi internal constructor() {

    @PublishedApi
    internal val entries = mutableListOf<NavEaseEntry<*>>()

    /**
     * Registers a [NavKey] subclass [K] with its composable screen content.
     *
     * The [content] lambda receives the typed key so route arguments are accessible directly:
     *
     * ```kotlin
     * screen<AppScreen.Home> { HomeScreen() }
     * screen<AppScreen.Detail> { key -> DetailScreen(id = key.id) }
     * ```
     *
     * Inside [content], use [io.github.alimsrepo.navease.runtime.composition.LocalNavEaseController]
     * to access the [io.github.alimsrepo.navease.runtime.navigation.NavController]:
     * ```kotlin
     * val nav = LocalNavEaseController.current
     * nav.navigate(AppScreen.Detail(id = "xyz"))
     * ```
     *
     * @param K The [NavKey] subclass for this screen. Must be annotated with `@Serializable`.
     */
    inline fun <reified K : NavKey> screen(noinline content: @Composable (K) -> Unit) {
        entries += NavEaseEntry(
            keyClass = K::class,
            serializer = serializer<K>(),
            content = content,
        )
    }

    @PublishedApi
    internal fun build(start: NavKey): NavEaseGraph {
        val savedStateConfig = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    entries.forEach { entry ->
                        subclass(
                            entry.keyClass as KClass<NavKey>,
                            entry.serializer as KSerializer<NavKey>,
                        )
                    }
                }
            }
        }
        return NavEaseGraph(
            start = start,
            savedStateConfig = savedStateConfig,
            factory = entries.associateBy { it.keyClass },
        )
    }
}

/**
 * Builds a [NavEaseGraph] — the **zero-rebuild** navigation graph DSL.
 *
 * ```kotlin
 * val appGraph = navEaseGraph(start = AppScreen.Home) {
 *     screen<AppScreen.Home> { HomeScreen() }
 *     screen<AppScreen.Detail> { key -> DetailScreen(id = key.id) }
 * }
 *
 * @Composable fun App() { NavEaseHost(appGraph) }
 * ```
 *
 * @param start  The [NavKey] instance placed on the back stack when the host first composes.
 * @param block  DSL block where screens are registered via [NavEaseGraphBuilder.screen].
 * @return A [NavEaseGraph] ready to pass to [NavEaseNavGraph][io.github.alimsrepo.navease.runtime.host.NavEaseNavGraph]
 *         or [NavEaseHost][io.github.alimsrepo.navease.runtime.host.NavEaseHost].
 */
inline fun navEaseGraph(
    start: NavKey,
    block: NavEaseGraphBuilder.() -> Unit,
): NavEaseGraph = NavEaseGraphBuilder().apply(block).build(start)

