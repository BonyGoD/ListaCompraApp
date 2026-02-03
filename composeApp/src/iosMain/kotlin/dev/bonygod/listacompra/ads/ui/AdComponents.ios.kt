package dev.bonygod.listacompra.ads.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotification
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIView
import platform.darwin.NSObject

// Nota: AdMobBannerView debe ser importado desde el framework Swift
// import AdMobKMPSwift.AdMobBannerView (esto se hace automáticamente)

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BannerAd(
    modifier: Modifier,
    adUnitId: String,
    onAdLoaded: () -> Unit,
    onAdFailedToLoad: (String) -> Unit
) {
    // Usar NotificationCenter para comunicarse con Swift, igual que con Interstitial
    val bannerId = remember { "banner_${adUnitId.hashCode()}" }

    DisposableEffect(Unit) {
        // Solicitar la creación del banner via NotificationCenter
        val userInfo: Map<Any?, *> = mapOf(
            "adUnitId" to adUnitId,
            "bannerId" to bannerId
        )

        NSNotificationCenter.defaultCenter.postNotificationName(
            "AdMobLoadBannerRequested",
            `object` = null,
            userInfo = userInfo
        )

        // Escuchar resultado
        var loadedObserver: Any? = null
        var failedObserver: Any? = null

        loadedObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = "AdMobBannerLoaded",
            `object` = null,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { notification: NSNotification? ->
                val id = notification?.userInfo?.get("bannerId") as? String
                if (id == bannerId) {
                    onAdLoaded()
                }
            }
        )

        failedObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = "AdMobBannerLoadFailed",
            `object` = null,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { notification: NSNotification? ->
                val id = notification?.userInfo?.get("bannerId") as? String
                if (id == bannerId) {
                    val error = notification?.userInfo?.get("error") as? String ?: "Unknown error"
                    onAdFailedToLoad(error)
                }
            }
        )

        onDispose {
            loadedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
            failedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        }
    }

    // Mostrar el banner usando UIKitView
    UIKitView(
        modifier = modifier,
        factory = {
            // Crear un UIView que contendrá el banner
            // El banner real se agregará desde Swift cuando esté listo
            val containerView = UIView()
            containerView.setTag(bannerId.hashCode().toLong())
            containerView
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun InterstitialAdTrigger(
    adUnitId: String,
    onAdShown: () -> Unit,
    onAdDismissed: () -> Unit,
    onAdFailedToShow: (String) -> Unit,
    content: @Composable (showAd: () -> Unit) -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var isReady by remember { mutableStateOf(false) }

    // Función para cargar el anuncio
    val loadAd = {
        if (!isLoading) {
            isLoading = true

            // Solicitar carga via NotificationCenter
            val userInfo: Map<Any?, *> = mapOf<Any?, Any?>("adUnitId" to adUnitId)
            NSNotificationCenter.defaultCenter.postNotificationName(
                "AdMobLoadInterstitialRequested",
                `object` = null,
                userInfo = userInfo
            )

            // Escuchar resultado
            var successObserver: Any? = null
            var failObserver: Any? = null

            successObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = "AdMobInterstitialLoaded",
                `object` = null,
                queue = NSOperationQueue.mainQueue,
                usingBlock = { _: NSNotification? ->
                    isLoading = false
                    isReady = true
                    successObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                    failObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                }
            )

            failObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = "AdMobInterstitialLoadFailed",
                `object` = null,
                queue = NSOperationQueue.mainQueue,
                usingBlock = { notification: NSNotification? ->
                    isLoading = false
                    isReady = false
                    val error = notification?.userInfo?.get("error") as? String ?: "Unknown error"
                    onAdFailedToShow(error)
                    successObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                    failObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                }
            )
        }
    }

    // Cargar el anuncio cuando se monta el composable
    LaunchedEffect(Unit) {
        loadAd()
    }

    // Función para mostrar el anuncio
    val showAd = {
        if (isReady) {
            // Solicitar mostrar el anuncio
            NSNotificationCenter.defaultCenter.postNotificationName(
                "AdMobShowInterstitialRequested",
                `object` = null
            )

            // Escuchar eventos
            var shownObserver: Any? = null
            var dismissedObserver: Any? = null
            var failedObserver: Any? = null

            shownObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = "AdMobInterstitialShown",
                `object` = null,
                queue = NSOperationQueue.mainQueue,
                usingBlock = { _: NSNotification? ->
                    onAdShown()
                }
            )

            dismissedObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = "AdMobInterstitialDismissed",
                `object` = null,
                queue = NSOperationQueue.mainQueue,
                usingBlock = { _: NSNotification? ->
                    isReady = false
                    onAdDismissed()
                    // Precargar el siguiente
                    loadAd()
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
                    isReady = false
                    val error = notification?.userInfo?.get("error") as? String ?: "Unknown error"
                    onAdFailedToShow(error)
                    loadAd()
                    shownObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                    dismissedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                    failedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                }
            )
        } else {
            onAdFailedToShow("Ad not ready")
        }
    }

    content(showAd)
}
