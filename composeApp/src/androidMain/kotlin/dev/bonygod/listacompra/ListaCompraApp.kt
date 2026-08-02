package dev.bonygod.listacompra

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dev.bonygod.crashlytics.kmp.core.CrashlyticsConfig
import dev.bonygod.crashlytics.kmp.core.CrashlyticsKMP
import dev.bonygod.crashlytics.kmp.core.CrashlyticsKeys
import dev.bonygod.listacompra.ads.AdConstants
import dev.bonygod.listacompra.ads.InterstitialAdManager
import dev.bonygod.listacompra.ads.getInterstitialAdUnitId
import dev.bonygod.listacompra.core.di.appModule
import dev.bonygod.listacompra.core.di.dataModule
import dev.bonygod.listacompra.core.di.initKoin
import dev.bonygod.listacompra.core.di.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class ListaCompraApp: Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicializar contexto de la plataforma
        initPlatform(this)

        // Inicializar AdMob
        MobileAds.initialize(this) {
            // Precargar el intersticial después de inicializar AdMob
            InterstitialAdManager.preloadAd(
                this,
                AdConstants.getInterstitialAdUnitId()
            )
        }

        CrashlyticsKMP.initialize(
            CrashlyticsConfig(
                isDebugBuild = getPlatform().isDebugBuild,
                defaultCustomKeys = mapOf(CrashlyticsKeys.APP_VERSION to (getPlatform().appVersion ?: "unknown"))
            )
        )

        initKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@ListaCompraApp)
            modules(appModule, viewModelsModule, dataModule)
        }
    }
}