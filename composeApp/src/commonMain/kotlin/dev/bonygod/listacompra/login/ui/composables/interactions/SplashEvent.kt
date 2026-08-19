package dev.bonygod.listacompra.login.ui.composables.interactions

sealed class SplashEvent {
    data object Retry : SplashEvent()
    data object GoToLogin : SplashEvent()
}
