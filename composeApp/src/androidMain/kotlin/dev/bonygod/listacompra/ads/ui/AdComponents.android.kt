package dev.bonygod.listacompra.ads.ui

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
    val context = LocalContext.current
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            AdView(ctx).apply {
                // Usar un banner adaptativo que ocupe todo el ancho
                val display = context.resources.displayMetrics
                val adWidthPixels = display.widthPixels.toFloat()
                val density = display.density
                val adWidth = (adWidthPixels / density).toInt()

                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth))
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

            android.util.Log.d("InterstitialAd", "🟡 Loading interstitial ad with ID: $adUnitId")

            com.google.android.gms.ads.interstitial.InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: com.google.android.gms.ads.interstitial.InterstitialAd) {
                        android.util.Log.d("InterstitialAd", "✅ Interstitial ad loaded successfully")
                        interstitialAd = ad
                        isLoading = false
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        android.util.Log.e("InterstitialAd", "❌ Failed to load ad: ${error.message}")
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
        android.util.Log.d("InterstitialAd", "🔵 showAd called. Ad ready: ${interstitialAd != null}")
        val activity = context as? android.app.Activity
        if (activity != null && interstitialAd != null) {
            android.util.Log.d("InterstitialAd", "🟢 Showing interstitial ad")
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    android.util.Log.d("InterstitialAd", "✅ Ad showed full screen content")
                    onAdShown()
                }

                override fun onAdDismissedFullScreenContent() {
                    android.util.Log.d("InterstitialAd", "👋 Ad dismissed")
                    interstitialAd = null
                    onAdDismissed()
                    // Precargar el siguiente anuncio
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    android.util.Log.e("InterstitialAd", "❌ Failed to show ad: ${error.message}")
                    interstitialAd = null
                    onAdFailedToShow(error.message)
                    loadAd()
                }
            }

            interstitialAd?.show(activity)
        } else {
            android.util.Log.e("InterstitialAd", "❌ Cannot show ad. Activity: $activity, Ad: $interstitialAd")
            onAdFailedToShow("Ad not ready or context is not an Activity")
        }
    }

    content(showAd)
}
