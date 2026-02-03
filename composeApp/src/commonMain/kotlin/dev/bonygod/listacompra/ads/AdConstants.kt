package dev.bonygod.listacompra.ads

import dev.bonygod.listacompra.BuildConfig

object AdConstants {
    // IDs de prueba de AdMob (para desarrollo/testing)
    // Estos son públicos de Google, no hay problema en dejarlos en el código
    const val TEST_BANNER_AD_UNIT_ID_ANDROID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID_ANDROID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID_ANDROID = "ca-app-pub-3940256099942544/5224354917"

    const val TEST_BANNER_AD_UNIT_ID_IOS = "ca-app-pub-3940256099942544/2934735716"
    const val TEST_INTERSTITIAL_AD_UNIT_ID_IOS = "ca-app-pub-3940256099942544/4411468910"
    const val TEST_REWARDED_AD_UNIT_ID_IOS = "ca-app-pub-3940256099942544/1712485313"

    // IDs de producción (ListaCompra App) - Desde BuildConfig (local.properties)
    // NO se suben al repositorio gracias a .gitignore
    private val PROD_BANNER_AD_UNIT_ID_ANDROID = BuildConfig.ADMOB_ANDROID_BANNER
    private val PROD_INTERSTITIAL_AD_UNIT_ID_ANDROID = BuildConfig.ADMOB_ANDROID_INTERSTITIAL

    private val PROD_BANNER_AD_UNIT_ID_IOS = BuildConfig.ADMOB_IOS_BANNER
    private val PROD_INTERSTITIAL_AD_UNIT_ID_IOS = BuildConfig.ADMOB_IOS_INTERSTITIAL

    // Modo de operación: true = usar IDs de prueba, false = usar IDs de producción
    private const val USE_TEST_ADS = true  // Cambiar a false cuando publiques

    // IDs activos (cambian según el modo)
    val BANNER_AD_UNIT_ID_ANDROID = if (USE_TEST_ADS) TEST_BANNER_AD_UNIT_ID_ANDROID else PROD_BANNER_AD_UNIT_ID_ANDROID
    val INTERSTITIAL_AD_UNIT_ID_ANDROID = if (USE_TEST_ADS) TEST_INTERSTITIAL_AD_UNIT_ID_ANDROID else PROD_INTERSTITIAL_AD_UNIT_ID_ANDROID

    val BANNER_AD_UNIT_ID_IOS = if (USE_TEST_ADS) TEST_BANNER_AD_UNIT_ID_IOS else PROD_BANNER_AD_UNIT_ID_IOS
    val INTERSTITIAL_AD_UNIT_ID_IOS = if (USE_TEST_ADS) TEST_INTERSTITIAL_AD_UNIT_ID_IOS else PROD_INTERSTITIAL_AD_UNIT_ID_IOS
}

// Funciones expect/actual para obtener el Ad Unit ID correcto según la plataforma
expect fun AdConstants.getBannerAdUnitId(): String
expect fun AdConstants.getInterstitialAdUnitId(): String

