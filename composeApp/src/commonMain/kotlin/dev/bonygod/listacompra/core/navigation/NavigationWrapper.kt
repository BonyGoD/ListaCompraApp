package dev.bonygod.listacompra.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.bonygod.listacompra.home.ui.screens.HomeScreen
import dev.bonygod.listacompra.login.ui.screens.ForgotPasswordScreen
import dev.bonygod.listacompra.login.ui.screens.LoginScreen
import dev.bonygod.listacompra.login.ui.screens.RegisterScreen
import org.koin.compose.koinInject

@Composable
fun NavigationWrapper(snackbarHostState: SnackbarHostState) {
    val navigator: Navigator = koinInject()

    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.goBack() },
        entryProvider = entryProvider {
            entry<Routes.Login> {
                LoginScreen(snackbarHostState = snackbarHostState)
            }
            entry<Routes.ForgotPassword> {
                ForgotPasswordScreen(snackbarHostState = snackbarHostState)
            }
            entry<Routes.Register> {
                RegisterScreen(snackbarHostState = snackbarHostState)
            }
            entry<Routes.Home> { entry ->
                val userId = entry.userId
                HomeScreen(snackbarHostState = snackbarHostState, userId)
            }
        },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(durationMillis = 250)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(durationMillis = 250)
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(durationMillis = 250)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(durationMillis = 250)
            )
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(durationMillis = 250)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(durationMillis = 250)
            )
        }
    )
}