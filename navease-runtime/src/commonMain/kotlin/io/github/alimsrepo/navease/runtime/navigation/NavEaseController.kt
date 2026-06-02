package io.github.alimsrepo.navease.runtime.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import io.github.alimsrepo.navease.internal.runtime.NavBackStack
import io.github.alimsrepo.navease.internal.runtime.NavKey
import io.github.alimsrepo.navease.runtime.transition.NavTransition

/**
 * Orchestrates navigation across the back stack.
 *
 * One instance is created per [io.github.alimsrepo.navease.runtime.presentation.NavEaseNavGraph]
 * call and is never shared between independent nav graphs — this prevents back-stack or result
 * cross-contamination in nested / multi-window setups.
 *
 * @param backStack         The [NavBackStack] that drives the [io.github.alimsrepo.navease.internal.navigation.ui.NavDisplay].
 * @param showExitDialog    Called when [back] is invoked at the root (back-stack size == 1).
 * @param defaultTransition The app-level fallback transition used when [navigate] is called
 *                          without an explicit [NavTransition] override. Supplied by
 *                          [io.github.alimsrepo.navease.runtime.presentation.NavEaseNavGraph]
 *                          from the `navTransition` parameter of `NavEaseHost`.
 */
@Stable
class NavEaseController(
    private val backStack: NavBackStack<NavKey>,
    private val showExitDialog: () -> Unit = {},
    internal var defaultTransition: NavTransition = NavTransition.Push,
) {

    /**
     * Result store scoped to this [NavEaseController] instance.
     *
     * A [androidx.compose.runtime.snapshots.SnapshotStateMap] is used so that writing a result
     * triggers Compose recomposition in any composable that reads it (via [resultOf]).
     * Keeping it here — rather than in a global singleton — ensures that multiple independent
     * [NavEaseController] instances (nested nav, bottom tabs, multi-window on Desktop) never
     * cross-contaminate each other.
     */
    internal val results = mutableStateMapOf<String, Any?>()

    /**
     * Per-push transition store.
     *
     * Every call to [navigate] records the resolved [NavTransition] against the pushed [NavKey].
     * [io.github.alimsrepo.navease.runtime.presentation.NavEaseNavGraph] reads this map inside its
     * `transitionSpec` / `popTransitionSpec` lambdas to animate each screen change with the
     * transition that was chosen *at call time*, falling back to [defaultTransition] when no
     * per-navigate override was given.
     *
     * The map uses regular (non-snapshot) mutability because it is only ever written inside
     * [navigate], which is already called from a Compose event handler — no extra snapshotting
     * is needed.
     *
     * Keys are the string representation of each [NavKey] (i.e. `navKey.toString()`), which
     * matches the `contentKey` that Navigation3 assigns to each [io.github.alimsrepo.navease.internal.runtime.NavEntry]
     * and therefore matches `Scene.key` inside the `transitionSpec` / `popTransitionSpec` lambdas.
     */
    internal val transitionStore = mutableMapOf<Any, NavTransition>()

    /**
     * Pops the current screen. If the back stack contains only the root screen,
     * [showExitDialog] is invoked instead.
     *
     * **Do not** remove the transition entry from [transitionStore] here.
     * [io.github.alimsrepo.navease.internal.navigation.ui.NavDisplay] reads `popTransitionSpec` **after** the backstack
     * mutation, so the entry must still be present when the pop animation starts.
     * Stale entries are evicted lazily on the next [navigate] call.
     */
    fun back() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        } else showExitDialog()
    }

    /**
     * Pushes [navKey] onto the back stack.
     *
     * @param finish        When `true`, the screen that was on top before the navigation is removed
     *                      (replace semantics).
     * @param singleTop     When `true`, navigation is skipped if [navKey] is already the top-most
     *                      entry (matched by runtime class).
     * @param navTransition Per-navigate animation override. When `null` (default), the app-level
     *                      [defaultTransition] is used. Pass an explicit [NavTransition] to use a
     *                      different animation for this one navigation only.
     */
    fun navigate(
        navKey: NavKey,
        finish: Boolean = false,
        singleTop: Boolean = false,
        navTransition: NavTransition? = null,
    ) {
        if (singleTop && backStack.isNotEmpty() && backStack.last()::class == navKey::class) {
            return
        }
        // Resolve and record the transition before the push so transitionSpec can read it
        // the moment NavDisplay animates the change.
        // Key is navKey.toString() which equals NavEntry.contentKey = Scene.key inside the
        // transitionSpec lambda — the only publicly accessible identifier for the entry.
        transitionStore[navKey.toString()] = navTransition ?: defaultTransition
        backStack.add(navKey)
        if (finish && backStack.size >= 2) {
            backStack.removeAt(backStack.size - 2)
        }
        // Lazy eviction: remove any transitionStore entries whose screens are no longer on
        // the back stack. This is safe to do here because the user cannot trigger a new
        // navigate() while a pop animation is still running — the previous animation is
        // always complete before the next interaction is possible. This prevents unbounded
        // map growth without breaking in-flight pop animations (see back() / popUpTo()).
        val liveKeys = backStack.mapTo(HashSet()) { it.toString() }
        transitionStore.keys.retainAll(liveKeys)
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
        // Do NOT remove from transitionStore here — the pop exit animations for the removed
        // screens still need their transition entries. Stale entries are cleaned up lazily
        // on the next navigate() call.
        repeat(removeCount) { backStack.removeLastOrNull() }
    }

    /**
     * Pops the back stack down to [index] (0 = root). All entries above [index] are removed.
     * Use [getHistory] to determine the target index before calling this.
     */
    fun popToIndex(index: Int) {
        val currentSize = backStack.size
        // Do NOT remove from transitionStore here — same reasoning as popUpTo().
        repeat(currentSize - index - 1) { backStack.removeLastOrNull() }
    }

    /** Returns a snapshot of the current back stack, oldest entry first. */
    fun getHistory(): List<NavKey> = backStack.toList()
}

