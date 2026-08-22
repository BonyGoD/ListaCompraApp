package dev.bonygod.listacompra.core.navigation

sealed class Routes {
    data object Splash : Routes()
    data object Login : Routes()
    data object ForgotPassword : Routes()
    data object Register : Routes()
    data class Home(val userId: String, val openLinkAccount: Boolean = false) : Routes()
    data class AdLoading(val userId: String) : Routes()
    data object MisListas : Routes()
    data object Alexa : Routes()
}