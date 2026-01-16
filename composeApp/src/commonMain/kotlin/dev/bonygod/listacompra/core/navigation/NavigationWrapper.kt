package dev.bonygod.listacompra.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import dev.bonygod.listacompra.core.navigation.routes.Route
import dev.bonygod.listacompra.home.ui.screens.HomeScreen
import dev.bonygod.listacompra.login.ui.screens.LoginScreen
import dev.bonygod.listacompra.login.ui.screens.RegisterScreen

@Composable
fun NavigationWrapper(snackbarHostState: SnackbarHostState) {
    val backStack = remember { mutableStateListOf<Any>(Route.Login) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when(key) {
                is Route.Login -> NavEntry(key) {
                    LoginScreen(snackbarHostState) {
                        backStack.add(Route.Register)
                    }
                }
                is Route.Register -> NavEntry(key) {
                    RegisterScreen(snackbarHostState)
                }
                is Route.Home -> NavEntry(key) {
                    HomeScreen(snackbarHostState)
                }
                else -> NavEntry(key = Unit) {

                }
            }
        }
    )
}