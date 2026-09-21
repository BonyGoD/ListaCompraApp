package dev.bonygod.listacompra.core.preferences

import android.content.Context
import android.content.SharedPreferences
import dev.bonygod.listacompra.appContext

private const val PREFS_NAME = "listacompra_preferencias_locales"

actual class PreferenciasLocales actual constructor() {
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    actual fun getBoolean(clave: String, porDefecto: Boolean): Boolean =
        prefs.getBoolean(clave, porDefecto)

    actual fun setBoolean(clave: String, valor: Boolean) {
        prefs.edit().putBoolean(clave, valor).apply()
    }
}
