package dev.bonygod.listacompra.core.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.bonygod.crashlytics.kmp.core.CrashReporter
import dev.bonygod.crashlytics.kmp.core.CrashlyticsKeys

class Navigator(private val crashReporter: CrashReporter) {
    private val _backStack: SnapshotStateList<Any> = mutableStateListOf(Routes.Login)

    val backStack: SnapshotStateList<Any>
        get() = _backStack

    fun navigateTo(routes: Routes) {
        _backStack.add(routes)
        reportScreen(routes)
    }

    fun goBack() {
        if (_backStack.size > 1) {
            _backStack.removeLastOrNull()
        }
        (_backStack.lastOrNull() as? Routes)?.let { reportScreen(it) }
    }

    fun replaceTo(routes: Routes) {
        if (_backStack.isNotEmpty()) {
            _backStack.removeLastOrNull()
        }
        _backStack.add(routes)
        reportScreen(routes)
    }

    fun clearAndNavigateTo(routes: Routes) {
        _backStack.clear()
        _backStack.add(routes)
        reportScreen(routes)
    }

    private fun reportScreen(routes: Routes) {
        crashReporter.setCustomKey(CrashlyticsKeys.SCREEN, routes::class.simpleName ?: "Unknown")
    }
}
