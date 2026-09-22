package dev.bonygod.listacompra

import android.app.Application
import dev.bonygod.admob.kmp.AdMobKMP
import dev.bonygod.admob.kmp.initializeAds
import dev.bonygod.crashlytics.kmp.core.CrashlyticsConfig
import dev.bonygod.crashlytics.kmp.core.CrashlyticsKMP
import dev.bonygod.crashlytics.kmp.core.CrashlyticsKeys
import dev.bonygod.listacompra.core.di.appModule
import dev.bonygod.listacompra.core.di.dataModule
import dev.bonygod.listacompra.core.di.initKoin
import dev.bonygod.listacompra.core.di.viewModelsModule
import dev.bonygod.listacompra.notificaciones.PushNotifications
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class ListaCompraApp: Application() {
    override fun onCreate() {
        super.onCreate()

        initPlatform(this)

        PushNotifications.initialize()

        CrashlyticsKMP.initialize(
            CrashlyticsConfig(
                isDebugBuild = getPlatform().isDebugBuild,
                defaultCustomKeys = mapOf(CrashlyticsKeys.APP_VERSION to (getPlatform().appVersion ?: "unknown"))
            )
        )

        initKoin {
            androidLogger(if (getPlatform().isDebugBuild) Level.DEBUG else Level.NONE)
            androidContext(this@ListaCompraApp)
            modules(appModule, viewModelsModule, dataModule)
        }

        AdMobKMP.initializeAds(this)
    }
}
