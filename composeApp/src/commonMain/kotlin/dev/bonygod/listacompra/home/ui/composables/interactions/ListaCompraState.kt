package dev.bonygod.listacompra.home.ui.composables.interactions

import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.ui.text.input.TextFieldValue
import dev.bonygod.listacompra.home.ui.model.ListaCompraUI
import dev.bonygod.listacompra.home.ui.model.UserUI

data class ListaCompraState(
    val loadingState: Boolean = false,
    val dialogState: Boolean = false,
    val error: String? = null,
    val listaCompraUI: ListaCompraUI = ListaCompraUI(),
    val user: UserUI = UserUI(),
    val editingProductId: String? = null,
    val editingText: TextFieldValue = TextFieldValue(""),
    val showErrorAlert: Boolean = false,
    val errorAlertTitle: String = "",
    val errorAlertMessage: String = "",
    val showSuccessAlert: Boolean = false,
    val successAlertTitle: String = "",
    val successAlertMessage: String = "",
    val showBottomSheet: Boolean = false,
    val newProductText: TextFieldValue = TextFieldValue(""),
    val showMenu: Boolean = false,
    val drawerState: DrawerState = DrawerState(initialValue = DrawerValue.Closed)
) {
    fun showMenu(): ListaCompraState {
        return copy(showMenu = true)
    }
    fun setUser(user: UserUI): ListaCompraState {
        return copy(user = user)
    }
    fun showLoading(show: Boolean = false): ListaCompraState {
        return copy(loadingState = show)
    }

    fun showDialog(show: Boolean = false): ListaCompraState {
        return copy(dialogState = show)
    }

    fun getListaCompraUI(nuevaLista: ListaCompraUI): ListaCompraState =
        copy(listaCompraUI = nuevaLista)

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
        return copy(editingProductId = productId, editingText = TextFieldValue(currentName))
    }

    fun updateEditingText(text: TextFieldValue): ListaCompraState {
        return copy(editingText = text)
    }

    fun saveEditedProduct(): ListaCompraState {
        val editingId = editingProductId ?: return this
        val updatedProductos = listaCompraUI.productos.map { producto ->
            if (producto.id == editingId) {
                producto.copy(nombre = editingText.text)
            } else {
                producto
            }
        }
        val updatedListaCompraUI = listaCompraUI.copy(productos = updatedProductos)
        return copy(
            listaCompraUI = updatedListaCompraUI,
            editingProductId = null,
            editingText = TextFieldValue("")
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
        return copy(editingProductId = null, editingText = TextFieldValue(""))
    }

    fun showBottomSheet(show: Boolean = false): ListaCompraState {
        return copy(showBottomSheet = show)
    }

    fun updateNewProductText(text: TextFieldValue): ListaCompraState {
        return copy(newProductText = text)
    }
}