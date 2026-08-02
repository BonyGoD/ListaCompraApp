package dev.bonygod.listacompra

import androidx.compose.ui.window.ComposeUIViewController
import dev.bonygod.crashlytics.kmp.core.CrashlyticsConfig
import dev.bonygod.crashlytics.kmp.core.CrashlyticsKMP
import dev.bonygod.crashlytics.kmp.core.CrashlyticsKeys
import dev.bonygod.listacompra.core.di.initKoin
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
fun MainViewController() = ComposeUIViewController(
    configure = {
        // `kotlin.native.Platform` fully qualificado: colisiona con nuestra propia
        // interfaz `Platform` (ver Platform.kt / getPlatform()).
        CrashlyticsKMP.initialize(
            CrashlyticsConfig(
                isDebugBuild = kotlin.native.Platform.isDebugBinary,
                defaultCustomKeys = mapOf(CrashlyticsKeys.APP_VERSION to (getPlatform().appVersion ?: "unknown"))
            )
        )
        initKoin()
    }
) {
    App()
}
