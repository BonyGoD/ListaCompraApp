package dev.bonygod.listacompra.notificaciones

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenNotificationSettingsURLString
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIDevice

// UIApplicationOpenNotificationSettingsURLString abre directamente la pantalla de
// notificaciones de la app, pero es de iOS 16. El mínimo del proyecto es iOS 15, así que
// hay que comprobar la versión en tiempo de ejecución antes de usarla: en un dispositivo
// anterior el enlace no es válido y falla en silencio. Se lee de systemVersion (String),
// como ya hace Platform.ios.kt, en vez de NSProcessInfo.isOperatingSystemAtLeastVersion,
// para no depender de un símbolo nuevo sin probar en esta rama.
private fun iosVersionAlMenos16(): Boolean {
    val major = UIDevice.currentDevice.systemVersion.substringBefore(".").toIntOrNull() ?: 0
    return major >= 16
}

@OptIn(ExperimentalForeignApi::class)
actual fun abrirAjustesNotificaciones() {
    val urlString = if (iosVersionAlMenos16()) {
        UIApplicationOpenNotificationSettingsURLString
    } else {
        UIApplicationOpenSettingsURLString
    }
    val url = NSURL(string = urlString)
    // La versión de un solo argumento está desactivada desde iOS 18: devuelve false sin
    // abrir nada.
    UIApplication.sharedApplication().openURL(url, options = emptyMap<Any?, Any?>(), completionHandler = null)
}
