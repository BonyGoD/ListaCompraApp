package dev.bonygod.listacompra.ads

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotification
import platform.Foundation.NSOperationQueue
import kotlin.coroutines.resume

actual class AdManager {
    private var isInitialized = false
    private var isInterstitialLoaded = false

    actual fun initialize() {
        if (!isInitialized) {
            // La inicialización se hace en Swift (iOSApp.swift)
            isInitialized = true
        }
    }

    actual fun loadInterstitial(callback: AdCallback?) {
        // Solicitar carga del intersticial via NotificationCenter
        val userInfo: Map<Any?, *> = mapOf<Any?, Any?>(
            "adUnitId" to AdConstants.INTERSTITIAL_AD_UNIT_ID_IOS
        )

        NSNotificationCenter.defaultCenter.postNotificationName(
            "AdMobLoadInterstitialRequested",
            `object` = null,
            userInfo = userInfo
        )

        // Escuchar resultado
        listenForInterstitialLoadResult(callback)
    }

    actual fun showInterstitial(callback: AdCallback?) {
        if (!isInterstitialLoaded) {
            callback?.onAdFailedToLoad("Ad not loaded")
            return
        }

        // Solicitar mostrar el intersticial
        NSNotificationCenter.defaultCenter.postNotificationName(
            "AdMobShowInterstitialRequested",
            `object` = null
        )

        // Escuchar eventos
        listenForInterstitialShowEvents(callback)
    }

    actual fun isInterstitialReady(): Boolean {
        return isInterstitialLoaded
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    private fun listenForInterstitialLoadResult(callback: AdCallback?) {
        var successObserver: Any? = null
        var failObserver: Any? = null

        successObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = "AdMobInterstitialLoaded",
            `object` = null,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { _: NSNotification? ->
                isInterstitialLoaded = true
                callback?.onAdLoaded()
                successObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                failObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
            }
        )

        failObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = "AdMobInterstitialLoadFailed",
            `object` = null,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { notification: NSNotification? ->
                isInterstitialLoaded = false
                val error = notification?.userInfo?.get("error") as? String ?: "Unknown error"
                callback?.onAdFailedToLoad(error)
                successObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                failObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
            }
        )
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    private fun listenForInterstitialShowEvents(callback: AdCallback?) {
        var shownObserver: Any? = null
        var dismissedObserver: Any? = null
        var failedObserver: Any? = null

        shownObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = "AdMobInterstitialShown",
            `object` = null,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { _: NSNotification? ->
                callback?.onAdShown()
            }
        )

        dismissedObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = "AdMobInterstitialDismissed",
            `object` = null,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { _: NSNotification? ->
                isInterstitialLoaded = false
                callback?.onAdDismissed()
                shownObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                dismissedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                failedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
            }
        )

        failedObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = "AdMobInterstitialShowFailed",
            `object` = null,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { notification: NSNotification? ->
                isInterstitialLoaded = false
                val error = notification?.userInfo?.get("error") as? String ?: "Unknown error"
                callback?.onAdFailedToLoad(error)
                shownObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                dismissedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                failedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
            }
        )
    }
}
