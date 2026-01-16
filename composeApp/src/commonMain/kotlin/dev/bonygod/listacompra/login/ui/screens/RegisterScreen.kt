package dev.bonygod.listacompra.login.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import dev.bonygod.listacompra.ScreenWrapper
import dev.bonygod.listacompra.login.ui.AuthViewModel
import dev.bonygod.listacompra.login.ui.composables.RegisterContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(snackbarHostState: SnackbarHostState) {
    val viewModel: AuthViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(state.value.authError) {
        val error = state.value.authError
        if (error.isNotEmpty()) {
            snackbarHostState.showSnackbar(message = error)
            viewModel.setState { setAuthError("") }
        }
    }

    ScreenWrapper(snackbarHostState = snackbarHostState) {
        RegisterContent(
            state = state.value,
            setEvent = viewModel::onEvent
        )
    }
}