package dev.bonygod.listacompra

import androidx.compose.ui.window.ComposeUIViewController
import dev.bonygod.admob.kmp.AdMobKMP
import dev.bonygod.admob.kmp.initializeAds
import dev.bonygod.crashlytics.kmp.core.CrashlyticsConfig
import dev.bonygod.crashlytics.kmp.core.CrashlyticsKMP
import dev.bonygod.crashlytics.kmp.core.CrashlyticsKeys
import dev.bonygod.listacompra.core.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        CrashlyticsKMP.initialize(
            CrashlyticsConfig(
                isDebugBuild = getPlatform().isDebugBuild,
                defaultCustomKeys = mapOf(CrashlyticsKeys.APP_VERSION to (getPlatform().appVersion ?: "unknown"))
            )
        )

        // initKoin() llama a AdMobKMP.configure(...) antes de arrancar Koin. El
        // SDK de Google Mobile Ads ya lo arrancó Swift (AdMobKMPBridge.start()
        // en AppDelegate) antes de que se cree este ViewController.
        initKoin()
        AdMobKMP.initializeAds()
    }
) {
    App()
}
