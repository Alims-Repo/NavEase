package io.github.alimsrepo.navease.runtime.data

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

@Stable
class NavController(
    private val backStack: NavBackStack<NavKey>,
    private val showExitDialog: () -> Unit
) {

    /**
     * Result store scoped to this [NavController] instance.
     *
     * Using a [androidx.compose.runtime.snapshots.SnapshotStateMap] ensures Compose
     * recomposition is triggered whenever a result is posted or consumed.
     * Keeping it here (instead of a global object) prevents result cross-contamination
     * between multiple independent [NavController] instances (nested nav, multi-window, etc.).
     */
    internal val results = mutableStateMapOf<String, Any?>()

    fun back() {
        if (backStack.size > 1)
            backStack.removeLastOrNull()
        else showExitDialog()
    }

    fun navigate(navKey: NavKey, finish: Boolean = false) {
        backStack.add(navKey)
        // Guard: only remove the previous entry when the stack actually has one to remove.
        // Without the check, calling navigate(finish=true) on an empty stack would
        // attempt removeAt(-1) and throw IndexOutOfBoundsException.
        if (finish && backStack.size >= 2) {
            backStack.removeAt(backStack.size - 2)
        }
    }

    fun getHistory(): List<NavKey> {
        return backStack.toList()
    }

    fun popToIndex(index: Int) {
        val currentSize = backStack.size
        repeat(currentSize - index - 1) {
            backStack.removeLastOrNull()
        }
    }
}