package io.github.alimsrepo.navease

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Provides navigation actions within a NavEase graph.
 *
 * Inject an instance of [NavEaseController] into your screen composables to
 * navigate between screens, pass results, or inspect the back stack.
 *
 * The controller is created internally by [NavEaseHostInternal] and passed to
 * every screen via the [NavEaseScreenFactory].
 */
@Stable
interface NavEaseController {

    /** A read-only snapshot of the current navigation back stack. */
    val backStack: List<NavEaseKey>

    /**
     * Navigate to the screen identified by [key].
     *
     * @param key         The destination route key.
     * @param singleTop   When `true`, navigation is skipped if [key] is already the top entry
     *                    (same class).  Useful for tabs or avoiding double-presses.
     */
    fun navigate(key: NavEaseKey, singleTop: Boolean = false)

    /**
     * Pop the top entry from the back stack (go back one screen).
     *
     * @return `true` if back navigation succeeded; `false` if the stack only has one entry.
     */
    fun back(): Boolean

    /**
     * Pop all entries above [key] in the back stack.
     *
     * @param key       The destination to pop up to.
     * @param inclusive When `true`, [key] itself is also removed from the stack.
     */
    fun popUpTo(key: NavEaseKey, inclusive: Boolean = false)

    /**
     * Pop the current screen and deliver a [result] to the previous screen.
     *
     * The previous screen can retrieve the result via [getResult].
     *
     * @param result Any value to pass back; can be `null`.
     */
    fun backWithResult(result: Any?)

    /**
     * Consume and return a result that was set by [backWithResult] from a child screen.
     *
     * The result is cleared after the first successful retrieval.
     *
     * @return The result cast to [T], or `null` if no result is pending.
     */
    fun <T> getResult(): T?
}

/**
 * Default implementation of [NavEaseController] backed by a [SnapshotStateList].
 * All state changes are tracked by Compose and trigger recomposition automatically.
 *
 * @param startDestination The initial route key shown when the host first composes.
 */
internal class NavEaseControllerImpl(
    startDestination: NavEaseKey,
) : NavEaseController {

    private val _backStack: SnapshotStateList<NavEaseKey> =
        mutableStateListOf(startDestination)

    override val backStack: List<NavEaseKey> get() = _backStack

    /**
     * `true` when the most recent navigation operation was a *back* action.
     * Used by the host to reverse the slide direction.
     */
    internal var isNavigatingBack: Boolean = false
        private set

    override fun navigate(key: NavEaseKey, singleTop: Boolean) {
        if (singleTop && _backStack.lastOrNull()?.let { it::class == key::class } == true) return
        isNavigatingBack = false
        _backStack.add(key)
    }

    override fun back(): Boolean {
        if (_backStack.size <= 1) return false
        isNavigatingBack = true
        _backStack.removeLastOrNull()
        return true
    }

    override fun popUpTo(key: NavEaseKey, inclusive: Boolean) {
        val index = _backStack.indexOfLast { it::class == key::class }
        if (index < 0) return
        val removeFrom = if (inclusive) index else index + 1
        if (removeFrom < _backStack.size) {
            isNavigatingBack = true
            _backStack.subList(removeFrom, _backStack.size).clear()
        }
    }

    private var _pendingResult: Any? = null

    override fun backWithResult(result: Any?) {
        _pendingResult = result
        back()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getResult(): T? {
        val result = _pendingResult as? T
        _pendingResult = null
        return result
    }

    /** Mutable access for the host composable. */
    internal val mutableBackStack: SnapshotStateList<NavEaseKey> get() = _backStack
}

