package io.github.alimsrepo.navease.runtime.annotations

/**
 * Marks an [ActivityScreen][io.github.alimsrepo.navease.runtime.screen.ActivityScreen]
 * subclass for automatic registration.
 *
 * ```kotlin
 * @AutoRegister
 * class HomeScreen : ActivityScreen<AppScreens.Home>() { /* … */ }
 * ```
 *
 * The annotation takes no arguments. The start destination belongs to the host, not to a
 * screen, so nested hosts can each pick their own.
 *
 * At build time KSP collects every annotated class and writes one file per module,
 * `AutoRegisterScreens.kt`, containing a `KSerializer` for each key, a `navEaseBootstrap()`
 * function that registers the screens, and a
 * [NavEaseHost][io.github.alimsrepo.navease.runtime.host.NavEaseHost] overload per sealed
 * root that bootstraps before hosting.
 *
 * **Requirements**, all checked at build time:
 * - the class extends `ActivityScreen<K>` directly and has a no-argument constructor;
 * - `K` is a subtype of [NavEaseRoot][io.github.alimsrepo.navease.runtime.NavEaseRoot];
 * - no two annotated classes in a module handle the same `K`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class AutoRegister
