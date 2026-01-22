package dev.bonygod.listacompra.core.navigation

sealed class Routes {
    data object Login : Routes()
    data object ForgotPassword : Routes()
    data object Register : Routes()
    data class Home(val userId: String) : Routes()
}