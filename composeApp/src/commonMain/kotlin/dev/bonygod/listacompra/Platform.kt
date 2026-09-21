package dev.bonygod.listacompra

interface Platform {
    val name: String
    val appVersion: String?
    val isDebugBuild: Boolean

    val idioma: String
}

expect fun getPlatform(): Platform

internal fun normalizeIdioma(raw: String?): String = when (raw?.lowercase()) {
    "ca" -> "ca"
    "en" -> "en"
    else -> "es"
}