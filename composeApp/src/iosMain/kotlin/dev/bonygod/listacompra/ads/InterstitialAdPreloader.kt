package dev.bonygod.listacompra.ads

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue

object InterstitialAdPreloader {

    /**
     * Obtiene el Ad Unit ID correcto para iOS (test o producción)
     */
    fun getAdUnitId(): String {
        return AdConstants.getInterstitialAdUnitId()
    }

    fun isInterstitialEnabled(): Boolean {
        return AdConstants.INTERSTITIAL_ENABLED
    }

    /**
     * Inicia la precarga del anuncio intersticial desde Kotlin
     * Esta función debe ser llamada desde Swift al iniciar la app
     */
    @OptIn(ExperimentalForeignApi::class)
    fun preloadInterstitial() {
        NSNotificationCenter.defaultCenter.postNotificationName(
            "AdPreloaderPreloadRequested",
            `object` = null,
            userInfo = mapOf("adUnitId" to getAdUnitId())
        )
        println("🟡 [iOS-Kotlin] Preload requested for: ${getAdUnitId()}")
    }
}
