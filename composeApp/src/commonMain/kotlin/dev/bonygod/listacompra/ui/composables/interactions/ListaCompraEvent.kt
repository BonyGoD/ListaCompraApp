package dev.bonygod.listacompra.ui.composables.interactions

sealed class ListaCompraEvent {
    data class BorrarProducto(val productId: String) : ListaCompraEvent()
    data object BorrarTodosLosProductos : ListaCompraEvent()
    data class ShowDialog(val show: Boolean) : ListaCompraEvent()
    data object ConfirmDelete : ListaCompraEvent()
    data object CancelDialog : ListaCompraEvent()
}