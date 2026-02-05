package dev.bonygod.listacompra

interface Platform {
    val name: String
    val appVersion: String?
}

expect fun getPlatform(): Platform