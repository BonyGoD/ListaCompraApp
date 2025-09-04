package dev.bonygod.listacompra

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.bonygod.listacompra.core.di.appModule
import dev.bonygod.listacompra.core.di.dataModule
import dev.bonygod.listacompra.core.di.initKoin
import dev.bonygod.listacompra.core.di.viewModelsModule

fun main() = application {
    initKoin {
        modules(appModule, viewModelsModule, dataModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Lista de Compra",
    ) {
        App()
    }
}