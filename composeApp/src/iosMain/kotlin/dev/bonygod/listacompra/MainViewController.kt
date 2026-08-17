package dev.bonygod.listacompra

import androidx.compose.ui.window.ComposeUIViewController
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
        initKoin()
    }
) {
    App()
}
