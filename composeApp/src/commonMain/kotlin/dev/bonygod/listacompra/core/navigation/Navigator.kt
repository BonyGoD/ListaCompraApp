package dev.bonygod.listacompra.core.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class Navigator {
    private val _backStack: SnapshotStateList<Any> = mutableStateListOf(Routes.Login)

    val backStack: SnapshotStateList<Any>
        get() = _backStack

    fun navigateTo(routes: Routes) {
        _backStack.add(routes)
    }

    fun goBack() {
        if (_backStack.size > 1) {
            _backStack.removeLastOrNull()
        }
    }

    fun replaceTo(routes: Routes) {
        if (_backStack.isNotEmpty()) {
            _backStack.removeLastOrNull()
        }
        _backStack.add(routes)
    }

    fun clearAndNavigateTo(routes: Routes) {
        _backStack.clear()
        _backStack.add(routes)
    }
}

