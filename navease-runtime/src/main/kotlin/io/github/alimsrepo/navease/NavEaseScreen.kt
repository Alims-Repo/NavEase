package io.github.alimsrepo.navease

import kotlin.reflect.KClass

/**
 * Annotates a `@Composable` function to register it as a NavEase screen.
 *
 * The KSP processor will discover all functions annotated with [NavEaseScreen] and
 * automatically generate:
 * - A `NavEaseGeneratedFactory` that maps route keys to their composable content.
 * - A `NavEaseHost` wrapper composable that wires everything together.
 *
 * ### Example
 * ```kotlin
 * @NavEaseScreen(route = AppScreens.Home::class)
 * @Composable
 * fun HomeScreen(navKey: AppScreens.Home, nav: NavEaseController) {
 *     // your UI here
 * }
 * ```
 *
 * @param route  The [NavEaseKey] subclass (route) this composable handles.
 * @param transition The [Transition] animation used when navigating to this screen.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class NavEaseScreen(
    val route: KClass<out NavEaseKey>,
    val transition: Transition = Transition.SLIDE,
)

