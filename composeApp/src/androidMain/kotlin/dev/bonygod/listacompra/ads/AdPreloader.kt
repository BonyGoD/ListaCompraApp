package dev.bonygod.listacompra.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Singleton para precargar y gestionar anuncios intersticiales en Android
 */
object AdPreloader {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun preloadAd(context: Context, adUnitId: String) {
        if (isLoading || interstitialAd != null) {
            android.util.Log.d("AdPreloader", "Ad already loading or loaded")
            return
        }

        isLoading = true
        android.util.Log.d("AdPreloader", "🟡 Preloading interstitial ad: $adUnitId")

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    android.util.Log.d("AdPreloader", "✅ Interstitial ad preloaded successfully")
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    android.util.Log.e("AdPreloader", "❌ Failed to preload ad: ${error.message}")
                    isLoading = false
                }
            }
        )
    }

    fun showAd(
        activity: Activity,
        onAdShown: () -> Unit = {},
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: (String) -> Unit = {}
    ) {
        if (interstitialAd == null) {
            android.util.Log.e("AdPreloader", "❌ No ad loaded to show")
            onAdFailedToShow("No ad loaded")
            return
        }

        android.util.Log.d("AdPreloader", "🟢 Showing preloaded interstitial ad")

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                android.util.Log.d("AdPreloader", "✅ Ad showed full screen content")
                onAdShown()
            }

            override fun onAdDismissedFullScreenContent() {
                android.util.Log.d("AdPreloader", "👋 Ad dismissed")
                interstitialAd = null
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                android.util.Log.e("AdPreloader", "❌ Failed to show ad: ${error.message}")
                interstitialAd = null
                onAdFailedToShow(error.message)
            }
        }

        interstitialAd?.show(activity)
    }

    fun isAdReady(): Boolean {
        return interstitialAd != null
    }
}
