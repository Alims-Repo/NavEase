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
// NavEaseHost — typed auto-discover @AutoRegister variant
// ─────────────────────────────────────────────────────────────────────────────

/**
 * **Typed auto-discover navigation host** — screens whose `NavKey` belongs to the
 * sealed root class [Root] are discovered automatically from the global
 * [NavEaseAutoRegistry], which is populated by KSP-generated code.
 *
 * No explicit `{ add(XxxScreen()) }` lambda needed — just supply the type parameter
 * and the start destination:
 *
 * ```kotlin
 * // Root host — discovers all screens whose NavKey is an AppScreens subtype
 * @Composable fun App() {
 *     NavEaseHost<AppScreens>(start = AppScreens.Splash, onExitRequest = { finish() })
 * }
 *
 * // Nested inner host — discovers only WizardStep screens
 * NavEaseHost<WizardStep>(
 *     start         = WizardStep.SelectRole,
 *     onExitRequest = { outerNav.back() },
 *     navTransition = NavTransition.Push,
 * )
 * ```
 *
 * @param Root                    The sealed [NavKey] root class whose screens to include
 *                                (e.g. `AppScreens` or `WizardStep`).
 * @param start                   The [NavKey] instance placed on the back stack first.
 * @param onExitRequest           Called when back is pressed on the root screen.
 * @param enableSharedTransitions `true` to enable shared-element transitions.
 * @param navTransition           Default screen-to-screen animation.
 */
@Composable
inline fun <reified Root : NavKey> NavEaseHost(
    start: Root,
    noinline onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) {
    NavEaseHostForRoot(
        rootClass              = Root::class,
        start                  = start,
        onExitRequest          = onExitRequest,
        enableSharedTransitions = enableSharedTransitions,
        navTransition          = navTransition,
    )
}

/**
 * Non-inline implementation backing [NavEaseHost] (typed auto-discover variant).
 *
 * Separated from the `inline reified` function so that the body (which contains
 * `@Composable` state calls like [remember]) is not inlined at every call site.
 */
@Composable
fun NavEaseHostForRoot(
    rootClass: KClass<*>,
    start: NavKey,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
) {
    val registry = NavEaseAutoRegistry

    val scope = remember(rootClass) {
        registry.ensureInitialized()
        check(registry.isInitialized) {
            "NavEase: Registry is empty. Rebuild the project " +
            "(./gradlew :shared:kspCommonMainKotlinMetadata) to generate @AutoRegister screen entries."
        }
        // Filter entries to only those belonging to this Root type
        val filtered = registry.entries.filter { it.rootKeyClass == rootClass }
        check(filtered.isNotEmpty()) {
            "NavEase: No @AutoRegister screens found for root type '${rootClass.simpleName}'. " +
            "Ensure the screen classes are annotated with @AutoRegister and the project has been rebuilt."
        }
        NavEaseScreenScope<NavKey>().also { s ->
            filtered.forEach { entry ->
                s.addUnchecked(entry.screen, entry.keyClass, entry.serializer)
            }
        }
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
        initialScreen        = start,
        savedStateConfig     = savedStateConfig,
        contentProvider      = { key ->
            @Suppress("UNCHECKED_CAST")
            val screen = factory[key::class] as? ActivityScreen<NavKey>
                ?: error(
                    "NavEase: No screen registered for '${key::class.simpleName}' " +
                    "in root '${rootClass.simpleName}'. " +
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

