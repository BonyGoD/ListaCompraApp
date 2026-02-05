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
import dev.bonygod.listacompra.ads.AdConstants
import dev.bonygod.listacompra.ads.getInterstitialAdUnitId
import dev.bonygod.listacompra.ads.ui.InterstitialAdTrigger
import dev.bonygod.listacompra.common.ui.FullScreenLoading
import dev.bonygod.listacompra.common.ui.state.SharedState
import dev.bonygod.listacompra.login.ui.AuthViewModel
import dev.bonygod.listacompra.login.ui.composables.LoginContent
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEffect
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEvent
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(snackbarHostState: SnackbarHostState) {
    val viewModel: AuthViewModel = koinViewModel()
    val sharedState: SharedState = koinInject()
    val isLoading = sharedState.isLoading.collectAsState()
    val state = viewModel.state.collectAsState()

    var shouldShowInterstitial by remember { mutableStateOf(false) }
    var pendingUserId by remember { mutableStateOf<String?>(null) }
    var adReadyToShow by remember { mutableStateOf(false) }

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
                    adReadyToShow = false
                }
            }
        }
    }

    if (isLoading.value) {
        FullScreenLoading()
    } else {
        ScreenWrapper(snackbarHostState = snackbarHostState) {
            if (shouldShowInterstitial && pendingUserId != null) {
                InterstitialAdTrigger(
                    adUnitId = AdConstants.getInterstitialAdUnitId(),
                    onAdShown = {
                        // Anuncio mostrado
                    },
                    onAdDismissed = {
                        // Navegar después de cerrar el anuncio
                        pendingUserId?.let { userId ->
                            viewModel.navigateToHome(userId)
                        }
                    },
                    onAdFailedToShow = { _ ->
                        // Si falla, navegar directamente
                        pendingUserId?.let { userId ->
                            viewModel.navigateToHome(userId)
                        }
                    }
                ) { showAd ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }

                    // Mostrar el anuncio cuando esté listo
                    LaunchedEffect(adReadyToShow) {
                        if (adReadyToShow) {
                            showAd()
                        } else {
                            // Esperar 3 segundos para que el anuncio cargue
                            kotlinx.coroutines.delay(3000)
                            adReadyToShow = true
                        }
                    }
                }
            } else {
                LoginContent(
                    state = state.value,
                    setEvent = viewModel::onEvent
                )
            }
        }
    }
}