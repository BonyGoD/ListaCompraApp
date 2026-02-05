package dev.bonygod.listacompra.login.ui.screens

import androidx.compose.runtime.Composable

@Composable
expect fun ShowPreloadedInterstitial(
    onAdShown: () -> Unit,
    onAdDismissed: () -> Unit,
    onAdFailedToShow: () -> Unit
)
