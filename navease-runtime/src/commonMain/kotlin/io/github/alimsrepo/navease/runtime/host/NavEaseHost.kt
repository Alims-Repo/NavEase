package io.github.alimsrepo.navease.runtime.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.alimsrepo.navease.runtime.composition.LocalNavEaseController
import io.github.alimsrepo.navease.runtime.graph.NavEaseGraph
import io.github.alimsrepo.navease.runtime.graph.NavEaseScreenScope
import io.github.alimsrepo.navease.runtime.registry.NavEaseAutoRegistry
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
import io.github.alimsrepo.navease.runtime.transition.NavTransition
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.reflect.KClass

// ─────────────────────────────────────────────────────────────────────────────
// NavEaseHost — DSL graph variant
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Zero-rebuild navigation host — **DSL graph variant**.
 *
 * Pass a [NavEaseGraph] built with [navEaseGraph][io.github.alimsrepo.navease.runtime.graph.navEaseGraph].
 * No code generation required.
 *
 * @param graph                   The navigation graph built via [navEaseGraph][io.github.alimsrepo.navease.runtime.graph.navEaseGraph].
 * @param onExitRequest           Called when back is pressed at the root screen.
 * @param enableSharedTransitions `true` to enable shared-element transitions.
 * @param navTransition           Default screen transition. Defaults to [NavTransition.Push].
 */
@Composable
fun NavEaseHost(
    graph: NavEaseGraph,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) {
    NavEaseNavGraph(
        graph = graph,
        onExitRequest = onExitRequest,
        enableSharedTransitions = enableSharedTransitions,
        navTransition = navTransition,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// NavEaseHost — ActivityScreen variant
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Zero-rebuild navigation host — **[ActivityScreen] variant**.
 *
 * Register screens via the trailing `screens` lambda using [NavEaseScreenScope.add].
 * Each [ActivityScreen] subclass receives a **typed** [navKey][ActivityScreen.Content] in
 * its [Content][ActivityScreen.Content] override — no casting or `.xxxArgs()` needed.
 *
 * ```kotlin
 * class DetailScreen : ActivityScreen<AppScreens.Detail>() {
 *     @Composable
 *     override fun Content(navKey: AppScreens.Detail, navController: NavController) {
 *         Text(navKey.id)   // ← typed, no casting
 *     }
 * }
 *
 * @Composable fun App() {
 *     NavEaseHost<AppScreens>(start = AppScreens.Home) {
 *         add(HomeScreen())
 *         add(AboutScreen())
 *         add(DetailScreen())
 *     }
 * }
 * ```
 *
 * @param Root                    The sealed [NavKey] root class (e.g. `AppScreens`).
 *                                Must be specified explicitly: `NavEaseHost<AppScreens>(...)`.
 * @param start                   The [NavKey] instance placed on the back stack first.
 * @param onExitRequest           Called when back is pressed at the root screen.
 * @param enableSharedTransitions `true` to enable shared-element transitions.
 * @param navTransition           Default screen transition. Defaults to [NavTransition.Push].
 * @param screens                 Registration block — call [NavEaseScreenScope.add] for each screen.
 */
@Composable
fun <Root : NavKey> NavEaseHost(
    start: Root,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
    screens: NavEaseScreenScope<Root>.() -> Unit,
) {
    val scope = remember { NavEaseScreenScope<Root>().apply(screens) }

    val factory: Map<KClass<*>, ActivityScreen<*>> = remember(scope) {
        scope.screens.associateBy { checkNotNull(it._keyClass) {
            "ActivityScreen '${it::class.simpleName}' has a null _keyClass. " +
            "Make sure it was registered via add(...) inside NavEaseHost { }."
        }}
    }

    val savedStateConfig = remember(scope) {
        SavedStateConfiguration {
            serializersModule = SerializersModule {
                @Suppress("UNCHECKED_CAST")
                polymorphic(NavKey::class) {
                    scope.serPairs.forEach { (kClass, ser) ->
                        subclass(kClass as KClass<NavKey>, ser as KSerializer<NavKey>)
                    }
                }
            }
        }
    }

    NavEaseNavGraphCore(
        initialScreen        = start,
        savedStateConfig     = savedStateConfig,
        contentProvider      = { key ->
            @Suppress("UNCHECKED_CAST")
            val screen = factory[key::class] as? ActivityScreen<NavKey>
                ?: error(
                    "NavEase: No screen registered for '${key::class.simpleName}'. " +
                    "Add it inside NavEaseHost<...>(...) { add(${key::class.simpleName}()) }."
                )
            val nav = LocalNavEaseController.current
                ?: error("NavEase: LocalNavEaseController is null — called outside NavEaseNavGraph.")
            screen.Content(key, nav)
        },
        onExitRequest        = onExitRequest,
        enableSharedTransitions = enableSharedTransitions,
        navTransition        = navTransition,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// NavEaseHost — universal @AutoRegister variant (zero config)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * **Universal `@AutoRegister` navigation host** — the simplest entry point.
 *
 * Screens are discovered automatically from the global [NavEaseAutoRegistry], which is
 * populated by KSP-generated code. No explicit type parameter, no `start` key, and no
 * screens registration lambda are needed.
 *
 * ```kotlin
 * @Composable fun App() {
 *     NavEaseHost(onExitRequest = { finish() })
 * }
 * ```
 *
 * @param onExitRequest           Called when back is pressed on the root screen.
 * @param enableSharedTransitions `true` to enable shared-element transitions.
 * @param navTransition           Default screen-to-screen animation.
 */
@Composable
fun NavEaseHost(
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) {
    val registry = NavEaseAutoRegistry

    val scope = remember {
        registry.ensureInitialized()
        check(registry.isInitialized) {
            "NavEase: No @AutoRegister screens found in the registry.\n" +
            "• On Android/JVM/Desktop: rebuild once — ./gradlew :shared:kspCommonMainKotlinMetadata\n" +
            "• On iOS/Native: call navEaseBootstrap() from your platform entry point before App():\n" +
            "    fun MainViewController() = ComposeUIViewController { navEaseBootstrap(); App() }\n" +
            "• On JS/WasmJS: ensure the shared module is properly imported."
        }
        NavEaseScreenScope<NavKey>().also { s ->
            registry.entries.forEach { entry ->
                s.addUnchecked(entry.screen, entry.keyClass, entry.serializer)
            }
        }
    }

    val startKey: NavKey = checkNotNull(registry.startKey) {
        "NavEase: Registry is populated but no start destination was found. " +
        "Annotate exactly one screen with @AutoRegister(startDestination = true)."
    }

    val factory: Map<KClass<*>, ActivityScreen<*>> = remember(scope) {
        scope.screens.associateBy { checkNotNull(it._keyClass) }
    }

    val savedStateConfig = remember(scope) {
        SavedStateConfiguration {
            serializersModule = SerializersModule {
                @Suppress("UNCHECKED_CAST")
                polymorphic(NavKey::class) {
                    scope.serPairs.forEach { (kClass, ser) ->
                        subclass(kClass as KClass<NavKey>, ser as KSerializer<NavKey>)
                    }
                }
            }
        }
    }

    NavEaseNavGraphCore(
        initialScreen        = startKey,
        savedStateConfig     = savedStateConfig,
        contentProvider      = { key ->
            @Suppress("UNCHECKED_CAST")
            val screen = factory[key::class] as? ActivityScreen<NavKey>
                ?: error(
                    "NavEase: No screen registered for '${key::class.simpleName}'. " +
                    "Ensure it is annotated with @AutoRegister and the project has been rebuilt."
                )
            val nav = LocalNavEaseController.current
                ?: error("NavEase: LocalNavEaseController is null — called outside NavEaseNavGraph.")
            screen.Content(key, nav)
        },
        onExitRequest        = onExitRequest,
        enableSharedTransitions = enableSharedTransitions,
        navTransition        = navTransition,
    )
}

