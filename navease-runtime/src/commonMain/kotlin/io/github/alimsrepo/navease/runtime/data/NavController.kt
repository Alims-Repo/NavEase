package io.github.alimsrepo.navease.runtime.data

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

@Stable
class NavController(
    private val backStack: NavBackStack<NavKey>,
    private val showExitDialog: () -> Unit
) {

    fun back() {
        if (backStack.size > 1)
            backStack.removeLastOrNull()
        else showExitDialog()
    }

    fun navigate(navKey: NavKey, finish: Boolean = false) {
        backStack.add(navKey)
        if (finish)
            backStack.removeAt(backStack.size - 2)
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