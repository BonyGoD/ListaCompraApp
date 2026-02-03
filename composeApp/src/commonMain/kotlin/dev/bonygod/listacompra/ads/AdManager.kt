package dev.bonygod.listacompra.ads

/**
 * Tipos de anuncios soportados
 */
enum class AdType {
    BANNER,
    INTERSTITIAL,
    REWARDED
}

/**
 * Estados de un anuncio
 */
sealed class AdState {
    data object Idle : AdState()
    data object Loading : AdState()
    data object Loaded : AdState()
    data class Failed(val error: String) : AdState()
    data object Shown : AdState()
    data object Dismissed : AdState()
}

/**
 * Callback para eventos de anuncios
 */
interface AdCallback {
    fun onAdLoaded() {}
    fun onAdFailedToLoad(error: String) {}
    fun onAdShown() {}
    fun onAdDismissed() {}
    fun onAdClicked() {}
}

/**
 * Configuración de anuncios
 */
data class AdConfig(
    val bannerAdUnitId: String,
    val interstitialAdUnitId: String,
    val rewardedAdUnitId: String? = null,
    val testMode: Boolean = true
)

/**
 * Interface principal para gestión de anuncios
 */
expect class AdManager {
    fun initialize()
    fun loadInterstitial(callback: AdCallback? = null)
    fun showInterstitial(callback: AdCallback? = null)
    fun isInterstitialReady(): Boolean
}
