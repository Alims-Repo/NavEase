@file:OptIn(ExperimentalSharedTransitionApi::class)

package io.github.alimsrepo.navease.runtime.host

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.alimsrepo.navease.internal.navigation.ui.NavDisplay
import io.github.alimsrepo.navease.internal.runtime.NavEntry
import io.github.alimsrepo.navease.internal.runtime.NavKey
import io.github.alimsrepo.navease.internal.runtime.rememberNavBackStack
import io.github.alimsrepo.navease.runtime.NavEaseRoot
import io.github.alimsrepo.navease.runtime.composition.LocalNavEaseController
import io.github.alimsrepo.navease.runtime.composition.LocalNavEaseSharedTransitionScope
import io.github.alimsrepo.navease.runtime.navigation.NavEaseController
import io.github.alimsrepo.navease.runtime.registry.NavEaseAutoRegistry
import io.github.alimsrepo.navease.runtime.screen.ActivityScreen
import io.github.alimsrepo.navease.runtime.transition.Animations
import io.github.alimsrepo.navease.runtime.transition.NavTransition
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.reflect.KClass

// ─────────────────────────────────────────────────────────────────────────────
// Public API
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Navigation host for every `@AutoRegister` screen whose key belongs to the sealed
 * root [Root].
 *
 * ```kotlin
 * @Composable
 * fun App() {
 *     NavEaseHost<AppScreens>(start = AppScreens.Splash, onExitRequest = { finish() })
 * }
 * ```
 *
 * Each host owns an independent back stack, controller and result store, so nested
 * hosts for different roots never see each other's screens.
 *
 * After a build, KSP generates a same-named overload for each of your roots. It is
 * strictly more specific than this declaration, so `NavEaseHost<AppScreens>(...)`
 * binds to the generated one, which initialises the screen registry before
 * delegating back to [NavEaseHostForRoot]. This declaration exists so your code
 * still resolves in the IDE *before* the first build, and so that a call which
 * somehow misses the generated overload fails with a clear message rather than a
 * blank screen.
 *
 * @param Root                    The sealed key class whose screens this host serves.
 * @param start                   The key placed on the back stack first.
 * @param onExitRequest           Called when back is pressed on the root screen.
 * @param enableSharedTransitions `true` to wrap the host in a `SharedTransitionLayout`.
 * @param navTransition           Default screen-to-screen animation.
 * @param onDestinationChanged    Called with [NavEaseController] as receiver whenever the
 *                                top of the back stack changes, including on first composition.
 */
@Composable
public inline fun <reified Root : NavEaseRoot> NavEaseHost(
    start: NavEaseRoot,
    noinline onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
    noinline onDestinationChanged: (NavEaseController.(NavKey) -> Unit)? = null,
) {
    require(start is Root) {
        "NavEase: start destination ${start::class.simpleName} is not a subtype of " +
            "${Root::class.simpleName}."
    }
    NavEaseHostForRoot(
        rootClass = Root::class,
        start = start,
        onExitRequest = onExitRequest,
        enableSharedTransitions = enableSharedTransitions,
        navTransition = navTransition,
        onDestinationChanged = onDestinationChanged,
    )
}

/**
 * Non-inline implementation backing [NavEaseHost].
 *
 * Also the target of the KSP-generated `NavEaseHost` overloads, which call
 * `navEaseBootstrap()` before delegating here. Prefer [NavEaseHost] in application
 * code; this entry point is public only so generated code can reach it.
 */
@Composable
public fun NavEaseHostForRoot(
    rootClass: KClass<*>,
    start: NavKey,
    onExitRequest: () -> Unit = {},
    enableSharedTransitions: Boolean = false,
    navTransition: NavTransition = NavTransition.Push,
    onDestinationChanged: (NavEaseController.(NavKey) -> Unit)? = null,
) {
    val screens: Map<KClass<*>, ActivityScreen<*>> = remember(rootClass) {
        NavEaseAutoRegistry.screensForRoot(rootClass)
    }

    val savedStateConfig = remember(rootClass) {
        val serializers = NavEaseAutoRegistry.serializersForRoot(rootClass)
        SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    serializers.forEach { (keyClass, serializer) ->
                        @Suppress("UNCHECKED_CAST")
                        subclass(keyClass as KClass<NavKey>, serializer as KSerializer<NavKey>)
                    }
                }
            }
        }
    }

    NavEaseDisplay(
        initialScreen = start,
        savedStateConfig = savedStateConfig,
        onExitRequest = onExitRequest,
        enableSharedTransitions = enableSharedTransitions,
        navTransition = navTransition,
        onDestinationChanged = onDestinationChanged,
    ) { key ->
        @Suppress("UNCHECKED_CAST")
        val screen = screens[key::class] as? ActivityScreen<NavKey>
            ?: error(
                "NavEase: no screen registered for '${key::class.simpleName}' under root " +
                    "'${rootClass.simpleName}'. Annotate its screen class with @AutoRegister " +
                    "and rebuild."
            )
        val controller = LocalNavEaseController.current
            ?: error("NavEase: LocalNavEaseController is null — Content() called outside a host.")
        screen.Content(key, controller)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Engine
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The rendering engine shared by every host: owns the back stack, the controller,
 * the transition wiring and the optional shared-element layer.
 *
 * @param content Renders the current key. [LocalNavEaseController] is already provided.
 */
@Composable
internal fun NavEaseDisplay(
    initialScreen: NavKey,
    savedStateConfig: SavedStateConfiguration,
    onExitRequest: () -> Unit,
    enableSharedTransitions: Boolean,
    navTransition: NavTransition,
    onDestinationChanged: (NavEaseController.(NavKey) -> Unit)?,
    content: @Composable (NavKey) -> Unit,
) {
    val backStack = rememberNavBackStack(configuration = savedStateConfig, initialScreen)

    val currentOnExitRequest by rememberUpdatedState(onExitRequest)

    val controller = remember(backStack) {
        NavEaseController(
            backStack = backStack,
            showExitDialog = { currentOnExitRequest() },
            defaultTransition = navTransition,
        )
    }

    SideEffect { controller.defaultTransition = navTransition }

    val currentOnDestinationChanged by rememberUpdatedState(onDestinationChanged)
    LaunchedEffect(backStack) {
        snapshotFlow { backStack.lastOrNull() }
            .distinctUntilChanged()
            .collect { key -> if (key != null) currentOnDestinationChanged?.invoke(controller, key) }
    }

    @Composable
    fun Display(sharedScope: SharedTransitionScope?) {
        CompositionLocalProvider(LocalNavEaseController provides controller) {
            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                backStack = backStack,
                sharedTransitionScope = sharedScope,
                transitionSpec = {
                    Animations.forward(controller.transitionStore[targetState.key] ?: navTransition)
                },
                popTransitionSpec = {
                    Animations.back(controller.transitionStore[initialState.key] ?: navTransition)
                },
                // Predictive back reuses the pop spec so the gesture and the button
                // animate identically, shared elements included.
                predictivePopTransitionSpec = {
                    Animations.back(controller.transitionStore[initialState.key] ?: navTransition)
                },
            ) { key ->
                NavEntry(key) { content(key) }
            }
        }
    }

    if (enableSharedTransitions) {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalNavEaseSharedTransitionScope provides this) {
                Display(sharedScope = this)
            }
        }
    } else {
        Display(sharedScope = null)
    }
}
