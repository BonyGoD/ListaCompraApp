package dev.bonygod.listacompra.login.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.bonygod.listacompra.ScreenWrapper
import dev.bonygod.listacompra.common.ui.FullScreenLoading
import dev.bonygod.listacompra.common.ui.state.SharedState
import dev.bonygod.listacompra.login.ui.AuthViewModel
import dev.bonygod.listacompra.login.ui.composables.LoginContent
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEffect
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEvent
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
expect fun showPreloadedInterstitial(
    onAdShown: () -> Unit,
    onAdDismissed: () -> Unit,
    onAdFailedToShow: (String) -> Unit
)

@Composable
fun LoginScreen(snackbarHostState: SnackbarHostState) {
    val viewModel: AuthViewModel = koinViewModel()
    val sharedState: SharedState = koinInject()
    val isLoading = sharedState.isLoading.collectAsState()
    val state = viewModel.state.collectAsState()

    var shouldShowInterstitial by remember { mutableStateOf(false) }
    var pendingUserId by remember { mutableStateOf<String?>(null) }

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
                }

                is AuthEffect.ShowInterstitialAndNavigate -> {
                    pendingUserId = effect.userId
                    shouldShowInterstitial = true
                }
            }
        }
    }

    if (isLoading.value) {
        FullScreenLoading()
    } else if (shouldShowInterstitial && pendingUserId != null) {
        // Mostrar el intersticial precargado
        showPreloadedInterstitial(
            onAdShown = {
                // Anuncio mostrado
            },
            onAdDismissed = {
                pendingUserId?.let { userId ->
                    viewModel.navigateToHome(userId)
                    shouldShowInterstitial = false
                }
            },
            onAdFailedToShow = { _ ->
                pendingUserId?.let { userId ->
                    viewModel.navigateToHome(userId)
                    shouldShowInterstitial = false
                }
            }
        )

        // Mostrar loading mientras se muestra el intersticial
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        ScreenWrapper(snackbarHostState = snackbarHostState) {
            LoginContent(
                state = state.value,
                setEvent = viewModel::onEvent
            )
        }
    }
}