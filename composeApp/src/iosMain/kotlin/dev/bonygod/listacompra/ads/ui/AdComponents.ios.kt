package dev.bonygod.listacompra.ads.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotification
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIView
import platform.UIKit.UIScreen
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
    UIKitView(
        modifier = modifier,
        factory = {
            // Crear el AdMobBannerView directamente
            // Swift lo expone como una clase accesible desde Kotlin
            createAdMobBannerView(adUnitId, onAdLoaded, onAdFailedToLoad)
        }
    )
}

// Función que crea el banner usando la clase Swift
@OptIn(ExperimentalForeignApi::class)
private fun createAdMobBannerView(
    adUnitId: String,
    onAdLoaded: () -> Unit,
    onAdFailedToLoad: (String) -> Unit
): UIView {
    println("🟢 [AdMob-Kotlin] createAdMobBannerView called with adUnitId: $adUnitId")

    // Enviar notificación para crear el banner de forma síncrona
    val bannerId = "banner_${adUnitId.hashCode()}"
    val containerView = UIView()
    containerView.setTag(bannerId.hashCode().toLong())

    // IMPORTANTE: Configurar un frame inicial para que el banner sea visible
    // El tamaño estándar de un banner de AdMob es 320x50
    val screenWidth = platform.UIKit.UIScreen.mainScreen.bounds.useContents { this.size.width }
    containerView.setFrame(platform.CoreGraphics.CGRectMake(0.0, 0.0, screenWidth, 50.0))

    println("🟢 [AdMob-Kotlin] Container created with bannerId: $bannerId, tag: ${bannerId.hashCode()}")
    println("🟢 [AdMob-Kotlin] Container frame: ${containerView.frame}")


    // Configurar observers antes de enviar la solicitud
    var loadedObserver: Any? = null
    var failedObserver: Any? = null

    loadedObserver = NSNotificationCenter.defaultCenter.addObserverForName(
        name = "AdMobBannerLoaded",
        `object` = null,
        queue = NSOperationQueue.mainQueue,
        usingBlock = { notification: NSNotification? ->
            val id = notification?.userInfo?.get("bannerId") as? String
            println("🟢 [AdMob-Kotlin] Received AdMobBannerLoaded for bannerId: $id")
            if (id == bannerId) {
                println("✅ [AdMob-Kotlin] Banner loaded callback!")
                onAdLoaded()
                loadedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                failedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
            }
        }
    )

    failedObserver = NSNotificationCenter.defaultCenter.addObserverForName(
        name = "AdMobBannerLoadFailed",
        `object` = null,
        queue = NSOperationQueue.mainQueue,
        usingBlock = { notification: NSNotification? ->
            val id = notification?.userInfo?.get("bannerId") as? String
            println("🟢 [AdMob-Kotlin] Received AdMobBannerLoadFailed for bannerId: $id")
            if (id == bannerId) {
                val error = notification?.userInfo?.get("error") as? String ?: "Unknown error"
                println("❌ [AdMob-Kotlin] Banner failed: $error")
                onAdFailedToLoad(error)
                loadedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
                failedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
            }
        }
    )

    println("🟢 [AdMob-Kotlin] Observers configured")

    // Enviar solicitud para crear el banner
    val userInfo: Map<Any?, *> = mapOf(
        "adUnitId" to adUnitId,
        "bannerId" to bannerId,
        "containerView" to containerView
    )

    println("🟢 [AdMob-Kotlin] Posting AdMobLoadBannerRequested notification...")
    println("🟢 [AdMob-Kotlin] userInfo keys: ${userInfo.keys}")
    println("🟢 [AdMob-Kotlin] containerView in userInfo: ${userInfo["containerView"]}")

    NSNotificationCenter.defaultCenter.postNotificationName(
        "AdMobLoadBannerRequested",
        `object` = null,
        userInfo = userInfo
    )

    println("✅ [AdMob-Kotlin] Notification posted, returning containerView")

    return containerView
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
