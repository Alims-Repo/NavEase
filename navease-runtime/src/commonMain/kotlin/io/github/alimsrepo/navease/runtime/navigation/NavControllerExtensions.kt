package io.github.alimsrepo.navease.runtime.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlin.reflect.KClass

/**
 * Posts [result] back to the previous screen and immediately pops the current screen.
 *
 * The result is stored in the [NavController] instance that owns this back stack, so multiple
 * independent navigation hosts never cross-contaminate each other's results.
 *
 * The map key is [KClass.simpleName] — supported on all KMP targets including JS and WASM
 * (unlike [KClass.qualifiedName] which is unavailable in Kotlin/JS). KSP generates result
 * class names that are unique per screen route (e.g. `LibraryDetailResult`, `SplashResult`),
 * so simple names are collision-free in practice.
 *
 * @throws IllegalStateException if [result] is an anonymous or local class (simpleName is null).
 */
fun NavController.backWithResult(result: Any) {
    val key = result::class.simpleName
        ?: error(
            "NavEase: Cannot use an anonymous or local class as a result. " +
            "Declare a named data class annotated with @NavEaseResult instead."
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
 * - [LaunchedEffect] runs after the first recomposition that observes a non-null pending value,
 *   removes the entry from [results] (one-shot), and commits it to a stable local [State].
 *
 * @param clazz The [KClass] of the expected result type.
 */
@Composable
fun <T : Any> NavController.resultOf(clazz: KClass<T>): State<T?> {
    val key = clazz.simpleName
        ?: error(
            "NavEase: Cannot observe results for an anonymous or local class. " +
            "Use a named data class annotated with @NavEaseResult instead."
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

    LaunchedEffect(pending) {
        if (pending != null) {
            results.remove(key)
            state.value = pending
        }
    }

    return state
}
