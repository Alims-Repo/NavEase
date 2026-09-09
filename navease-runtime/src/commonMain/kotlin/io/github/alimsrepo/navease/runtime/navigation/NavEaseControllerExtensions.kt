package io.github.alimsrepo.navease.runtime.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlin.reflect.KClass

/**
 * Posts [result] back to the previous screen and immediately pops the current screen.
 *
 * The result is stored in the [NavEaseController] instance that owns this back stack, so multiple
 * independent navigation hosts never cross-contaminate each other's results.
 *
 * The map key is [KClass.simpleName] — supported on all KMP targets including JS and WASM
 * (unlike [KClass.qualifiedName] which is unavailable in Kotlin/JS). KSP generates result
 * class names that are unique per screen route (e.g. `LibraryDetailResult`, `SplashResult`),
 * so simple names are collision-free in practice.
 *
 * @throws IllegalStateException if [result] is an anonymous or local class (simpleName is null).
 */
fun NavEaseController.backWithResult(result: Any) {
    val key = result::class.simpleName
        ?: error(
            "NavEase: Cannot use an anonymous or local class as a result. " +
            "Declare a named data class or object instead."
        )
    results[key] = result
    back()
}

/**
 * Observes the result of type [T] returned by a child screen via [backWithResult].
 *
 * The result is **consumed exactly once**: the returned [State] transitions from `null` to the
 * received value and stays at that value until the composable leaves the composition.
 *
 * Implementation notes:
 * - Uses [KClass.simpleName] as the map key — available on all KMP targets (Android, iOS,
 *   Desktop, JS, WASM). [KClass.qualifiedName] is not supported in Kotlin/JS.
 * - The [results] map is a [androidx.compose.runtime.snapshots.SnapshotStateMap], so reading
 *   it inside [derivedStateOf] creates a reactive dependency — recomposition triggers
 *   automatically the moment [backWithResult] puts a matching entry.
 * - [SideEffect] runs synchronously after every successful recomposition. When a pending value
 *   is observed it is removed from [results] and committed to the stable local [State] **in
 *   the same frame** — eliminating the one-frame null window that a [LaunchedEffect] would
 *   introduce.
 *
 * Prefer the inline reified overload [resultOf] when the type is known at the call site:
 * ```kotlin
 * val result by navController.resultOf<ProfileScreen.Result>()
 * ```
 *
 * @param clazz The [KClass] of the expected result type.
 */
@Composable
fun <T : Any> NavEaseController.resultOf(clazz: KClass<T>): State<T?> {
    val key = clazz.simpleName
        ?: error(
            "NavEase: Cannot observe results for an anonymous or local class. " +
            "Use a named data class or object instead."
        )

    // Stable local state that holds the consumed value for the caller to read.
    // Persists across recompositions until the composable leaves the composition.
    val state = remember(key) { mutableStateOf<T?>(null) }

    // Reactively watch the instance-scoped results map. When a matching entry appears
    // (posted by backWithResult from a child screen), transfer it to local state and
    // immediately remove it from the map so it is consumed exactly once.
    val pending = remember(key) {
        derivedStateOf {
            @Suppress("UNCHECKED_CAST")
            results[key] as? T
        }
    }.value

    // SideEffect runs after every successful recomposition — same frame as the observation.
    // This avoids the one-frame null window that LaunchedEffect would cause (coroutine launch
    // is deferred to after the composition phase).
    SideEffect {
        if (pending != null) {
            results.remove(key)
            state.value = pending
        }
    }

    return state
}

/**
 * Inline reified convenience overload of [resultOf].
 *
 * Observes the result of type [T] posted by a child screen via [backWithResult].
 * The result is consumed exactly once — see [resultOf] for full semantics.
 *
 * ## Pattern for [io.github.alimsrepo.navease.runtime.screen.ActivityScreen]
 *
 * Define the result as a nested `data class` inside the child screen, then observe it
 * in the parent screen using this function:
 *
 * ```kotlin
 * // ── Child screen — posts a result before popping ─────────────────────────
 * class ProfileScreen : ActivityScreen<AppScreens.Profile>() {
 *
 *     data class Result(val selectedUserId: Int)
 *
 *     @Composable
 *     override fun Content(navKey: AppScreens.Profile, navController: NavController) {
 *         Button(onClick = {
 *             navController.backWithResult(Result(selectedUserId = 42))
 *         }) { Text("Select") }
 *     }
 * }
 *
 * // ── Parent screen — observes the result ──────────────────────────────────
 * class HomeScreen : ActivityScreen<AppScreens.Home>() {
 *     @Composable
 *     override fun Content(navKey: AppScreens.Home, navController: NavController) {
 *
 *         val result by navController.resultOf<ProfileScreen.Result>()
 *
 *         result?.let { Text("Selected user #${it.selectedUserId}") }
 *
 *         Button(onClick = { navController.navigate(AppScreens.Profile) }) {
 *             Text("Open Profile")
 *         }
 *     }
 * }
 * ```
 *
 * @param T The expected result type. Typically a nested `data class` inside the child screen.
 */
@Composable
inline fun <reified T : Any> NavEaseController.resultOf(): State<T?> = resultOf(T::class)
