package dev.bonygod.listacompra.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.FullScreenContentCallback
import dev.bonygod.listacompra.appContext

actual class AdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isInitialized = false

    actual fun initialize() {
        if (!isInitialized) {
            MobileAds.initialize(appContext) {}
            isInitialized = true
        }
    }

    actual fun loadInterstitial(callback: AdCallback?) {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            appContext,
            AdConstants.INTERSTITIAL_AD_UNIT_ID_ANDROID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    callback?.onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    callback?.onAdFailedToLoad(error.message)
                }
            }
        )
    }

    actual fun showInterstitial(callback: AdCallback?) {
        val context = appContext as? android.app.Activity

        if (context == null) {
            callback?.onAdFailedToLoad("Context is not an Activity")
            return
        }

        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    callback?.onAdShown()
                }

                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    callback?.onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    callback?.onAdFailedToLoad(error.message)
                }
            }

            ad.show(context)
        } ?: run {
            callback?.onAdFailedToLoad("Ad not loaded")
        }
    }

    actual fun isInterstitialReady(): Boolean {
        return interstitialAd != null
    }
}
