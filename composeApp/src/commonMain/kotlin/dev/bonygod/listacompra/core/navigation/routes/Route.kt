package dev.bonygod.listacompra.core.navigation.routes

sealed class Route {
    data object Login: Route()
    data object Register: Route()
    data object Home: Route()
}