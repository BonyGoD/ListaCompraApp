package dev.bonygod.listacompra.login.ui.screens

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import dev.bonygod.listacompra.ads.InterstitialAdManager
import kotlinx.coroutines.delay

@Composable
actual fun ShowPreloadedInterstitial(
    onAdShown: () -> Unit,
    onAdDismissed: () -> Unit,
    onAdFailedToShow: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var attempted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (activity == null) {
            android.util.Log.e("ShowPreloadedInterstitial", "❌ Context is not an Activity")
            onAdFailedToShow()
            return@LaunchedEffect
        }

        // Esperar un poco si el anuncio está cargándose
        var attempts = 0
        while (!InterstitialAdManager.isAdReady() && attempts < 10) {
            android.util.Log.d("ShowPreloadedInterstitial", "⏳ Waiting for ad to load... attempt $attempts")
            delay(300)
            attempts++
        }

        if (InterstitialAdManager.isAdReady() && !attempted) {
            attempted = true
            InterstitialAdManager.showAd(
                activity = activity,
                onAdShown = onAdShown,
                onAdDismissed = onAdDismissed,
                onAdFailedToShow = { error ->
                    android.util.Log.e("ShowPreloadedInterstitial", "❌ Failed: $error")
                    onAdFailedToShow()
                }
            )
        } else {
            android.util.Log.e("ShowPreloadedInterstitial", "❌ Ad not ready after waiting")
            onAdFailedToShow()
        }
    }
}
