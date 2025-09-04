package dev.bonygod.listacompra.ui.composables.interactions

sealed class ListaCompraEvent {
    data object ActualizarProductos : ListaCompraEvent()
}