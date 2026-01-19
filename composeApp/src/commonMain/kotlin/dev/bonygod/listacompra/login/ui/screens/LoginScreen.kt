package dev.bonygod.listacompra.login.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import dev.bonygod.listacompra.ScreenWrapper
import dev.bonygod.listacompra.common.CustomDialog
import dev.bonygod.listacompra.common.FullScreenLoading
import dev.bonygod.listacompra.login.ui.AuthViewModel
import dev.bonygod.listacompra.login.ui.composables.LoginContent
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEffect
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(snackbarHostState: SnackbarHostState) {
    val viewModel: AuthViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.ShowError -> {
                    snackbarHostState.showSnackbar(message = effect.message)
                }
                is AuthEffect.DismissDialog -> {
                    viewModel.onEvent(AuthEvent.DismissDialog)
                }
                is AuthEffect.NavigateTo -> {
                    // Navigation is handled directly in the ViewModel via the navigator
                }
            }
        }
    }

    if(state.value.showDialog){
        CustomDialog(viewModel::onEvent)
    }

    if (state.value.isLoading) {
        FullScreenLoading()
    } else {
        ScreenWrapper(snackbarHostState = snackbarHostState) {
            LoginContent(
                state = state.value,
                setEvent = viewModel::onEvent
            )
        }
    }
}