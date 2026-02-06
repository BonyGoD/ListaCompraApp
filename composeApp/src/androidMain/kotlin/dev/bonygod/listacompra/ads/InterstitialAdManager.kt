package dev.bonygod.listacompra.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun preloadAd(context: Context, adUnitId: String) {
        if (interstitialAd != null || isLoading) {
            android.util.Log.d("InterstitialAdManager", "⚠️ Ad already loaded or loading")
            return
        }

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        android.util.Log.d("InterstitialAdManager", "🟡 Preloading ad...")

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    android.util.Log.d("InterstitialAdManager", "✅ Ad preloaded successfully")
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    android.util.Log.e("InterstitialAdManager", "❌ Failed to preload ad: ${error.message}")
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    fun showAd(
        activity: Activity,
        onAdShown: () -> Unit,
        onAdDismissed: () -> Unit,
        onAdFailedToShow: (String) -> Unit
    ) {
        val ad = interstitialAd
        if (ad != null) {
            android.util.Log.d("InterstitialAdManager", "🟢 Showing preloaded ad")
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    android.util.Log.d("InterstitialAdManager", "✅ Ad showed")
                    onAdShown()
                }

                override fun onAdDismissedFullScreenContent() {
                    android.util.Log.d("InterstitialAdManager", "👋 Ad dismissed")
                    interstitialAd = null
                    onAdDismissed()
                    // Precargar el siguiente anuncio
                    preloadAd(activity, AdConstants.getInterstitialAdUnitId())
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    android.util.Log.e("InterstitialAdManager", "❌ Failed to show ad: ${error.message}")
                    interstitialAd = null
                    onAdFailedToShow(error.message)
                    // Precargar de nuevo
                    preloadAd(activity, AdConstants.getInterstitialAdUnitId())
                }
            }
            ad.show(activity)
        } else {
            android.util.Log.e("InterstitialAdManager", "❌ Ad not ready")
            onAdFailedToShow("Ad not preloaded")
        }
    }

    fun isAdReady(): Boolean = interstitialAd != null
}
