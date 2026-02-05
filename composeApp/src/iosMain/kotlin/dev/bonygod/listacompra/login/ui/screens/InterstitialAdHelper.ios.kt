package dev.bonygod.listacompra.login.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun showPreloadedInterstitial(
    onAdShown: () -> Unit,
    onAdDismissed: () -> Unit,
    onAdFailedToShow: (String) -> Unit
) {
    // TODO: Implementar para iOS
    LaunchedEffect(Unit) {
        onAdFailedToShow("Not implemented for iOS yet")
    }
}
