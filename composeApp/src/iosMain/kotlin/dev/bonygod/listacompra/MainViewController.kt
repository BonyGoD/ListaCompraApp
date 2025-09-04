package dev.bonygod.listacompra

import androidx.compose.ui.window.ComposeUIViewController
import dev.bonygod.listacompra.core.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() }
) {
    App()
}