package io.github.alimsrepo.navease.runtime.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Orchestrates navigation across the back stack.
 *
 * One instance is created per [io.github.alimsrepo.navease.runtime.presentation.NavEaseNavGraph]
 * call and is never shared between independent nav graphs — this prevents back-stack or result
 * cross-contamination in nested / multi-window setups.
 *
 * @param backStack      The [NavBackStack] that drives the [androidx.navigation3.ui.NavDisplay].
 * @param showExitDialog Called when [back] is invoked at the root (back-stack size == 1).
 *                       Typical uses: show a "Do you want to exit?" dialog, or call
 *                       `Activity.finish()` on Android. Defaults to a no-op so platforms
 *                       without an explicit exit concept (iOS, web) need no configuration.
 */
@Stable
class NavController(
    private val backStack: NavBackStack<NavKey>,
    private val showExitDialog: () -> Unit = {}
) {

    /**
     * Result store scoped to this [NavController] instance.
     *
     * A [androidx.compose.runtime.snapshots.SnapshotStateMap] is used so that writing a result
     * triggers Compose recomposition in any composable that reads it (via [resultOf]).
     * Keeping it here — rather than in a global singleton — ensures that multiple independent
     * [NavController] instances (nested nav, bottom tabs, multi-window on Desktop) never
     * cross-contaminate each other.
     */
    internal val results = mutableStateMapOf<String, Any?>()

    /**
     * Pops the current screen. If the back stack contains only the root screen,
     * [showExitDialog] is invoked instead (allowing the host to show a confirmation or exit).
     */
    fun back() {
        if (backStack.size > 1)
            backStack.removeLastOrNull()
        else showExitDialog()
    }

    /**
     * Pushes [navKey] onto the back stack.
     *
     * @param finish    When `true`, the screen that was on top *before* the navigation is removed
     *                  from the stack (i.e. the current screen is "replaced" rather than "layered").
     *                  Requires at least 2 entries after the push — guarded against empty stacks.
     * @param singleTop When `true`, navigation is skipped if [navKey] is already the top-most
     *                  entry (matched by runtime class). Useful for tabs and splash→home transitions
     *                  where accidental double-pushes would create duplicate stack entries.
     */
    fun navigate(navKey: NavKey, finish: Boolean = false, singleTop: Boolean = false) {
        if (singleTop && backStack.isNotEmpty() && backStack.last()::class == navKey::class) {
            return
        }
        backStack.add(navKey)
        if (finish && backStack.size >= 2) {
            backStack.removeAt(backStack.size - 2)
        }
    }

    /**
     * Pops the back stack until [key]'s screen is reached.
     *
     * Matching is by runtime class — all argument values are ignored (only the screen type
     * matters, consistent with how [navigate] with `singleTop` works).
     *
     * @param key       A [NavKey] instance whose **class** identifies the destination to pop to.
     *                  The argument values on [key] are not used for matching.
     * @param inclusive When `false` (default), the destination screen is kept on the stack.
     *                  When `true`, the destination screen itself is also removed.
     */
    fun popUpTo(key: NavKey, inclusive: Boolean = false) {
        val index = backStack.indexOfLast { it::class == key::class }
        if (index < 0) return
        val removeCount = if (inclusive) backStack.size - index else backStack.size - index - 1
        repeat(removeCount) { backStack.removeLastOrNull() }
    }

    /**
     * Pops the back stack down to [index] (0 = root). All entries above [index] are removed.
     * Use [getHistory] to determine the target index before calling this.
     */
    fun popToIndex(index: Int) {
        val currentSize = backStack.size
        repeat(currentSize - index - 1) {
            backStack.removeLastOrNull()
        }
    }

    /** Returns a snapshot of the current back stack, oldest entry first. */
    fun getHistory(): List<NavKey> = backStack.toList()
}

