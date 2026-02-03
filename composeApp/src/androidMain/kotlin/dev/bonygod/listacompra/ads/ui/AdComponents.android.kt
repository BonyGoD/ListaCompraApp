package dev.bonygod.listacompra.ads.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import android.annotation.SuppressLint

@SuppressLint("MissingPermission")
@Composable
actual fun BannerAd(
    modifier: Modifier,
    adUnitId: String,
    onAdLoaded: () -> Unit,
    onAdFailedToLoad: (String) -> Unit
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId

                adListener = object : com.google.android.gms.ads.AdListener() {
                    override fun onAdLoaded() {
                        onAdLoaded()
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        onAdFailedToLoad(error.message)
                    }
                }

                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
actual fun InterstitialAdTrigger(
    adUnitId: String,
    onAdShown: () -> Unit,
    onAdDismissed: () -> Unit,
    onAdFailedToShow: (String) -> Unit,
    content: @Composable (showAd: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var interstitialAd by remember { mutableStateOf<com.google.android.gms.ads.interstitial.InterstitialAd?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Función para cargar el anuncio
    val loadAd: () -> Unit = {
        if (!isLoading) {
            isLoading = true
            val adRequest = AdRequest.Builder().build()

            com.google.android.gms.ads.interstitial.InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: com.google.android.gms.ads.interstitial.InterstitialAd) {
                        interstitialAd = ad
                        isLoading = false
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isLoading = false
                        onAdFailedToShow(error.message)
                    }
                }
            )
        }
    }

    // Cargar el anuncio cuando se monta el composable
    LaunchedEffect(Unit) {
        loadAd()
    }

    // Función para mostrar el anuncio
    val showAd: () -> Unit = {
        val activity = context as? android.app.Activity
        if (activity != null && interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    onAdShown()
                }

                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onAdDismissed()
                    // Precargar el siguiente anuncio
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    onAdFailedToShow(error.message)
                    loadAd()
                }
            }

            interstitialAd?.show(activity)
        } else {
            onAdFailedToShow("Ad not ready or context is not an Activity")
        }
    }

    content(showAd)
}
