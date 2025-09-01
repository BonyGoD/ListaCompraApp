package dev.bonygod.listacompra

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform