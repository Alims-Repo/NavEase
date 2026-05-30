@file:Suppress("UNCHECKED_CAST")

package io.github.alimsrepo.navease.runtime.presentation

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
 * Binds a [NavKey] subclass [K] to its composable content and serializer.
 *
 * Created internally by [NavEaseGraphBuilder.screen]; not intended to be
 * instantiated directly.
 */
class NavEaseEntry<K : NavKey> @PublishedApi internal constructor(
    @PublishedApi internal val keyClass: KClass<K>,
    @PublishedApi internal val serializer: KSerializer<K>,
    @PublishedApi internal val content: @Composable (K) -> Unit,
)

/**
 * A compiled navigation graph ready to be passed to [NavEaseNavGraph] or [NavEaseHost].
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
     * Inside [content], use [LocalNavEaseController] to access the [NavController]:
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
 * Builds a [NavEaseGraph] — the **zero-rebuild** alternative to KSP annotation processing.
 *
 * ## The problem it solves
 * With `@NavEaseScreen` annotations, every new screen forces a full rebuild before the IDE
 * can resolve the generated `navigateToXxx()` extensions. `navEaseGraph` replaces all of
 * that with a plain Kotlin DSL — the IDE resolves it instantly, no rebuild required.
 *
 * ## Migration from KSP
 *
 * **Before (KSP):**
 * ```kotlin
 * @NavEaseScreen("home", startDestination = true)
 * class HomeScreen : NavScreen() { ... }
 *
 * @NavEaseScreen("detail")
 * class DetailScreen : NavScreen() {
 *     @NavEaseArgs data class Args(val id: String)
 *     override fun Content(navKey: NavKey, navController: NavController) {
 *         val args = navKey.detailArgs()
 *         // ...
 *     }
 * }
 *
 * // In App.kt — KSP-generated:
 * NavEaseHost()
 * navController.navigateToDetail(id = "abc")
 * ```
 *
 * **After (DSL — no rebuild ever):**
 * ```kotlin
 * // 1. Define NavKeys — sealed class, no annotations needed:
 * @Serializable sealed class AppScreen : NavKey {
 *     @Serializable data object Home : AppScreen()
 *     @Serializable data class Detail(val id: String) : AppScreen()
 * }
 *
 * // 2. Build the graph once — e.g. top-level val or inside remember():
 * val appGraph = navEaseGraph(start = AppScreen.Home) {
 *     screen<AppScreen.Home> { HomeScreen() }
 *     screen<AppScreen.Detail> { key -> DetailScreen(id = key.id) }
 * }
 *
 * // 3. Host — no generated code, no rebuild:
 * @Composable fun App() { NavEaseHost(appGraph) }
 *
 * // 4. Navigate — immediately available in the IDE:
 * val nav = LocalNavEaseController.current
 * nav.navigate(AppScreen.Detail(id = "abc"))
 * ```
 *
 * The [SavedStateConfiguration] (required for back-stack state restoration) is built
 * automatically from the registered screens — no manual `SerializersModule` needed.
 *
 * @param start  The [NavKey] instance placed on the back stack when the host first composes.
 * @param block  DSL block where screens are registered via [NavEaseGraphBuilder.screen].
 * @return A [NavEaseGraph] ready to pass to [NavEaseNavGraph] or [NavEaseHost].
 */
inline fun navEaseGraph(
    start: NavKey,
    block: NavEaseGraphBuilder.() -> Unit,
): NavEaseGraph = NavEaseGraphBuilder().apply(block).build(start)

