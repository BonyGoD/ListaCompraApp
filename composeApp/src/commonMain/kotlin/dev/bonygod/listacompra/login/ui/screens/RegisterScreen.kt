package dev.bonygod.listacompra.login.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import dev.bonygod.listacompra.ScreenWrapper
import dev.bonygod.listacompra.common.FullScreenLoading
import dev.bonygod.listacompra.login.ui.AuthViewModel
import dev.bonygod.listacompra.login.ui.composables.RegisterContent
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEffect
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(snackbarHostState: SnackbarHostState) {
    val viewModel: AuthViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.ShowError -> {
                    snackbarHostState.showSnackbar(message = effect.message)
                }
                is AuthEffect.DismissDialog -> {
                    state.value.showDialog(false)
                }
                is AuthEffect.NavigateTo -> {
                    // Navigation is handled directly in the ViewModel via the navigator
                }
            }
        }
    }

    if (state.value.isLoading) {
        FullScreenLoading()
    } else {
        ScreenWrapper(snackbarHostState = snackbarHostState) {
            RegisterContent(
                state = state.value,
                setEvent = viewModel::onEvent
            )
        }
    }
}