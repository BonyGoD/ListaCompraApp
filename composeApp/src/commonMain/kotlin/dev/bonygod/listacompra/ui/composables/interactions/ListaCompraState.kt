package dev.bonygod.listacompra.ui.composables.interactions

import dev.bonygod.listacompra.ui.model.ListaCompraUI

data class ListaCompraState(
    val loadingState: Boolean = false,
    val dialogState: Boolean = false,
    val error: String? = null,
    val listaCompraUI: ListaCompraUI = ListaCompraUI(),
    val editingProductId: String? = null,
    val editingText: String = "",
    val showErrorAlert: Boolean = false,
    val errorAlertTitle: String = "",
    val errorAlertMessage: String = "",
    val showSuccessAlert: Boolean = false,
    val successAlertTitle: String = "",
    val successAlertMessage: String = ""
) {
    fun showLoading(show: Boolean = false): ListaCompraState {
        return copy(loadingState = show)
    }

    fun showDialog(show: Boolean = false): ListaCompraState {
        return copy(dialogState = show)
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

    fun removeProducto(productId: String): ListaCompraState {
        val updatedProductos = listaCompraUI.productos.filter { it.id != productId }
        val updatedListaCompraUI = listaCompraUI.copy(productos = updatedProductos)
        return copy(listaCompraUI = updatedListaCompraUI)
    }

    fun clearAllProductos(): ListaCompraState {
        val updatedListaCompraUI = listaCompraUI.copy(productos = emptyList())
        return copy(listaCompraUI = updatedListaCompraUI)
    }

    fun startEditingProduct(productId: String, currentName: String): ListaCompraState {
        return copy(editingProductId = productId, editingText = currentName)
    }

    fun updateEditingText(text: String): ListaCompraState {
        return copy(editingText = text)
    }

    fun saveEditedProduct(): ListaCompraState {
        val editingId = editingProductId ?: return this
        val updatedProductos = listaCompraUI.productos.map { producto ->
            if (producto.id == editingId) {
                producto.copy(nombre = editingText)
            } else {
                producto
            }
        }
        val updatedListaCompraUI = listaCompraUI.copy(productos = updatedProductos)
        return copy(
            listaCompraUI = updatedListaCompraUI,
            editingProductId = null,
            editingText = ""
        )
    }

    fun showErrorAlert(title: String, message: String): ListaCompraState {
        return copy(
            showErrorAlert = true,
            errorAlertTitle = title,
            errorAlertMessage = message
        )
    }

    fun hideErrorAlert(): ListaCompraState {
        return copy(
            showErrorAlert = false,
            errorAlertTitle = "",
            errorAlertMessage = ""
        )
    }

    fun showSuccessAlert(title: String, message: String): ListaCompraState {
        return copy(
            showSuccessAlert = true,
            successAlertTitle = title,
            successAlertMessage = message
        )
    }

    fun hideSuccessAlert(): ListaCompraState {
        return copy(
            showSuccessAlert = false,
            successAlertTitle = "",
            successAlertMessage = ""
        )
    }

    fun cancelEditing(): ListaCompraState {
        return copy(editingProductId = null, editingText = "")
    }
}