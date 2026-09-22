package dev.bonygod.listacompra

import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.UIKit.UIDevice
import kotlin.experimental.ExperimentalNativeApi

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val appVersion: String
        get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "N/A"
    @OptIn(ExperimentalNativeApi::class)
    override val isDebugBuild: Boolean
        get() = kotlin.native.Platform.isDebugBinary
    override val idioma: String
        get() = normalizeIdioma(NSLocale.currentLocale.languageCode)
}

actual fun getPlatform(): Platform = IOSPlatform()