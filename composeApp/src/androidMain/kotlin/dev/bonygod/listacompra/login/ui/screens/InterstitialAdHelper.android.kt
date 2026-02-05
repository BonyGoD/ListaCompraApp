package dev.bonygod.listacompra.login.ui.screens

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import dev.bonygod.listacompra.ads.AdConstants
import dev.bonygod.listacompra.ads.AdPreloader
import dev.bonygod.listacompra.ads.getInterstitialAdUnitId

@Composable
actual fun showPreloadedInterstitial(
    onAdShown: () -> Unit,
    onAdDismissed: () -> Unit,
    onAdFailedToShow: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        if (activity != null) {
            if (AdPreloader.isAdReady()) {
                // Mostrar el anuncio precargado
                AdPreloader.showAd(
                    activity = activity,
                    onAdShown = onAdShown,
                    onAdDismissed = {
                        onAdDismissed()
                        // Precargar el siguiente anuncio
                        AdPreloader.preloadAd(activity, AdConstants.getInterstitialAdUnitId())
                    },
                    onAdFailedToShow = {
                        onAdFailedToShow(it)
                        // Intentar precargar de nuevo
                        AdPreloader.preloadAd(activity, AdConstants.getInterstitialAdUnitId())
                    }
                )
            } else {
                // Si no hay anuncio precargado, fallar directamente
                onAdFailedToShow("Ad not preloaded")
            }
        } else {
            onAdFailedToShow("Context is not an Activity")
        }
    }
}
