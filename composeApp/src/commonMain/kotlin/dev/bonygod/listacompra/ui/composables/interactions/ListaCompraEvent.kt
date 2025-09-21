package dev.bonygod.listacompra.ui.composables.interactions

sealed class ListaCompraEvent {
    data class BorrarProducto(val productId: String) : ListaCompraEvent()
    data object BorrarTodosLosProductos : ListaCompraEvent()
}