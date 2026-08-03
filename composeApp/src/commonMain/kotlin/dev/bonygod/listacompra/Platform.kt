package dev.bonygod.listacompra

interface Platform {
    val name: String
    val appVersion: String?
    val isDebugBuild: Boolean
}

expect fun getPlatform(): Platform