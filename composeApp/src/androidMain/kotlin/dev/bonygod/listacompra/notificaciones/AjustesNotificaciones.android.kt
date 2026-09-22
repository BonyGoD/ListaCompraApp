package dev.bonygod.listacompra.notificaciones

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import dev.bonygod.listacompra.appContext

actual fun abrirAjustesNotificaciones() {
    // ACTION_APP_NOTIFICATION_SETTINGS existe desde la API 26; el minSdk del proyecto es
    // 24, así que por debajo hace falta caer a los ajustes generales de la app.
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, appContext.packageName)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", appContext.packageName, null)
        }
    }
    // appContext no es una Activity: sin este flag, startActivity lanza IllegalStateException.
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    appContext.startActivity(intent)
}
