package dev.bonygod.listacompra.ui.composables.interactions

import dev.bonygod.listacompra.ui.model.ListaCompraUI

data class ListaCompraState(
    val loadingState: Boolean = false,
    val error: String? = null,
    val listaCompraUI: ListaCompraUI = ListaCompraUI()
) {
    fun showLoading(show: Boolean = false): ListaCompraState {
        return copy(loadingState = show)
    }

    fun getListaCompraUI(nuevaLista: ListaCompraUI): ListaCompraState =
        copy(listaCompraUI = nuevaLista)

    fun updateUnidadesProducto(productId: String, unidades: Int): ListaCompraState {
        val updatedListaCompraUI = listaCompraUI.updateUnidadesProducto(productId, unidades)
        return copy(listaCompraUI = updatedListaCompraUI)
    }

    fun updateListaCompra(listaCompraUI: ListaCompraUI): ListaCompraState {
        return copy(listaCompraUI = listaCompraUI)
    }
}