package dev.bonygod.listacompra.core.preferences

/**
 * Persistencia local mínima, por dispositivo (no por cuenta). El proyecto no traía
 * ninguna dependencia de almacenamiento local (ni DataStore ni SharedPreferences ni
 * NSUserDefaults), así que para guardar un booleano no compensa añadir una librería:
 * un expect/actual propio, como ya se hizo con las push o con AdMob.
 */
expect class PreferenciasLocales() {
    fun getBoolean(clave: String, porDefecto: Boolean): Boolean
    fun setBoolean(clave: String, valor: Boolean)
    fun getLong(clave: String, porDefecto: Long): Long
    fun setLong(clave: String, valor: Long)
}
