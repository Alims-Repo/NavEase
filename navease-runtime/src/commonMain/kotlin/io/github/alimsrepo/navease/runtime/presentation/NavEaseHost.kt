package io.github.alimsrepo.navease.runtime.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

// ─────────────────────────────────────────────────────────────────────────────
// NavEaseScreenScope — registration DSL used inside NavEaseHost { }
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Receiver of the `screens` lambda inside [NavEaseHost].
 *
 * Use [add] to register each [ActivityScreen]. The reified type parameter is inferred
 * automatically from the screen class:
 *
 * ```kotlin
 * NavEaseHost<AppScreens>(start = AppScreens.Home) {
 *     add(HomeScreen())    // K inferred as AppScreens.Home
 *     add(AboutScreen())   // K inferred as AppScreens.About
 *     add(DetailScreen())  // K inferred as AppScreens.Detail
 * }
 * ```
 */
class NavEaseScreenScope<Root : NavKey> @PublishedApi internal constructor() {

    @PublishedApi internal val screens     = mutableListOf<ActivityScreen<*>>()
    @PublishedApi internal val serPairs    = mutableListOf<Pair<KClass<*>, KSerializer<*>>>()

    /**
     * Registers an [ActivityScreen] for the [NavKey] subclass [K].
     *
     * [K] is inferred from the screen's generic parameter — no explicit type argument needed:
     * ```kotlin
     * add(DetailScreen())   // K = AppScreens.Detail, inferred automatically
     * ```
     *
     * @param screen An instance of your [ActivityScreen] subclass.
     */
    inline fun <reified K : Root> add(screen: ActivityScreen<K>) {
        screen._keyClass    = K::class
        screen._serializer  = serializer<K>()
        screens  += screen
        serPairs += K::class to serializer<K>()
    }

    /**
     * Non-inline variant used internally by [NavEaseAutoRegistry] to populate the scope
     * from pre-resolved KSP registry entries (no reified type parameter needed).
     */
    internal fun addUnchecked(
        screen: ActivityScreen<*>,
        keyClass: KClass<*>,
        serializer: KSerializer<*>,
    ) {
        screen._keyClass   = keyClass
        screen._serializer = serializer
        screens  += screen
        serPairs += keyClass to serializer
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// autoRegisterScreens() — runtime no-op stub
// ─────────────────────────────────────────────────────────────────────────────

/**
 * No-op stub for `autoRegisterScreens()`.
 *
 * Before KSP runs this function is the only candidate, so referencing it inside a
 * `NavEaseHost<Root> { }` block compiles cleanly without errors.  After KSP generates
 * the more-specific typed overload (`fun NavEaseScreenScope<AppScreens>.autoRegisterScreens()`)
 * Kotlin's overload resolution picks that one, and this stub is never called.
 *
 * **Do not call this stub directly.** It is purely a compile-time shim.
 */
@Suppress("UNUSED_PARAMETER")
fun NavEaseScreenScope<*>.autoRegisterScreens() {
    // no-op — superseded by the KSP-generated typed extension after first build
}

// ─────────────────────────────────────────────────────────────────────────────
// NavEaseHost — DSL graph variant (existing)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Zero-rebuild navigation host — DSL graph variant.
 *
 * Pass a [NavEaseGraph] built with [navEaseGraph]. No code generation required.
 *
 * @param graph                   The navigation graph built via [navEaseGraph].
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
// NavEaseHost — ActivityScreen variant (new)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Zero-rebuild navigation host — **[ActivityScreen] variant**.
 *
 * Register screens via the trailing `screens` lambda using [NavEaseScreenScope.add].
 * Each [ActivityScreen] subclass receives a **typed** [navKey][ActivityScreen.Content] in
 * its [Content][ActivityScreen.Content] override — no casting or `.xxxArgs()` needed.
 *
 * ```kotlin
 * // Define screens with typed navKey:
 * class DetailScreen : ActivityScreen<AppScreens.Detail>() {
 *     @Composable
 *     override fun Content(navKey: AppScreens.Detail, navController: NavController) {
 *         Text(navKey.id)   // ← typed, no casting
 *     }
 * }
 *
 * // Host — explicit sealed-class type param tells the scope which root NavKey to expect:
 * @Composable fun App() {
 *     NavEaseHost<AppScreens>(start = AppScreens.Home) {
 *         add(HomeScreen())
 *         add(AboutScreen())
 *         add(DetailScreen())
 *     }
 * }
 * ```
 *
 * The [SavedStateConfiguration] (for back-stack restoration after process death) is built
 * automatically from the registered screens — no manual `SerializersModule` required.
 *
 * @param Root                    The sealed [NavKey] root class (e.g. `AppScreens`).
 *                                Must be specified explicitly: `NavEaseHost<AppScreens>(...)`.
 * @param start                   The [NavKey] instance placed on the back stack first.
 * @param onExitRequest           Called when back is pressed at the root screen.
 *                                Defaults to a no-op (suitable for iOS / Web targets).
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
    // Build scope once; screen instances are stable after initial composition.
    val scope = remember { NavEaseScreenScope<Root>().apply(screens) }

    // Map from NavKey runtime class → ActivityScreen for O(1) dispatch.
    val factory: Map<KClass<*>, ActivityScreen<*>> = remember(scope) {
        scope.screens.associateBy { checkNotNull(it._keyClass) {
            "ActivityScreen '${it::class.simpleName}' has a null _keyClass. " +
            "Make sure it was registered via add(...) inside NavEaseHost { }."
        }}
    }

    // Auto-build SavedStateConfiguration from registered screens so back-stack
    // entries survive process death without any manual SerializersModule boilerplate.
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
// NavEaseHost — universal @AutoRegister variant (zero type-param, zero screens lambda)
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
 * **Platform notes**
 * - **Android / JVM**: auto-discovery works out of the box — the registry is populated
 *   transparently on first composition via class loading.
 * - **iOS / Desktop / Web**: call [navEaseInit] once before the first composition, or
 *   use the typed overload with [autoRegisterScreens] which works on every platform:
 *   ```kotlin
 *   NavEaseHost<AppScreens>(start = AppScreens.Splash, onExitRequest = { ... }) {
 *       autoRegisterScreens()
 *   }
 *   ```
 *
 * **Before KSP runs**: this composable compiles without errors. The registry will be
 * empty, producing a clear runtime message asking you to rebuild the project.
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

    // Build a NavEaseScreenScope<NavKey> from the global registry entries.
    // All operations are unchecked but type-safe via KSP generation guarantees.
    val scope = remember {
        registry.ensureInitialized()
        check(registry.isInitialized) {
            "NavEase: No @AutoRegister screens found in the registry.\n" +
            "• Rebuild the project to run KSP code generation.\n" +
            "• On non-JVM platforms, also call navEaseInit() from your platform entry " +
            "point, or use NavEaseHost<Root>(start = ...) { autoRegisterScreens() }."
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
        initialScreen       = startKey,
        savedStateConfig    = savedStateConfig,
        contentProvider     = { key ->
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
        onExitRequest       = onExitRequest,
        enableSharedTransitions = enableSharedTransitions,
        navTransition       = navTransition,
    )
}

