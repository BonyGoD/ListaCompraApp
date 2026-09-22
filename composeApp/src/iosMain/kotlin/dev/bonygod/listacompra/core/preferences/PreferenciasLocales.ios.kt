package dev.bonygod.listacompra.core.preferences

import platform.Foundation.NSUserDefaults

// A diferencia de NSLocale.currentLocale (categoría NSLocaleCreation, requiere import
// aparte del miembro), standardUserDefaults/boolForKey/setBool:forKey: están declarados
// en la interfaz principal de NSUserDefaults, así que se resuelven como miembros normales
// de la clase con solo importar NSUserDefaults.
actual class PreferenciasLocales actual constructor() {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getBoolean(clave: String, porDefecto: Boolean): Boolean {
        return if (defaults.objectForKey(clave) != null) defaults.boolForKey(clave) else porDefecto
    }

    actual fun setBoolean(clave: String, valor: Boolean) {
        defaults.setBool(valor, forKey = clave)
    }

    actual fun getLong(clave: String, porDefecto: Long): Long {
        return if (defaults.objectForKey(clave) != null) defaults.integerForKey(clave) else porDefecto
    }

    actual fun setLong(clave: String, valor: Long) {
        defaults.setInteger(valor, forKey = clave)
    }
}
