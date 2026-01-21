package dev.bonygod.listacompra.core.CustomFailures

sealed class HomeFailures(message: String) : Exception(message) {
    class BlankOrNullListId : HomeFailures("No tienes listas asociadas")
}