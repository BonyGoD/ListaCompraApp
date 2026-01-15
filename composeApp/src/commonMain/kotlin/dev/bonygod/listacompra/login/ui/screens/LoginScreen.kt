package dev.bonygod.listacompra.login.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import dev.bonygod.listacompra.ScreenWrapper
import dev.bonygod.listacompra.login.ui.AuthViewModel
import dev.bonygod.listacompra.login.ui.composables.LoginContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(snackbarHostState: SnackbarHostState) {
    val viewModel: AuthViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()


    LaunchedEffect(state.value.loginError) {
        val error = state.value.loginError
        if (error.isNotEmpty()) {
            snackbarHostState.showSnackbar(message = error)
            viewModel.setState { setLoginError("") }
        }
    }

    ScreenWrapper(snackbarHostState = snackbarHostState) {
        LoginContent(
            state = state.value,
            setEvent = viewModel::onEvent
        )
    }
}