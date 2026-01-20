package dev.bonygod.listacompra.home.ui.composables.interactions

import androidx.compose.ui.text.input.TextFieldValue

sealed class ListaCompraEvent {
    data class BorrarProducto(val productId: String) : ListaCompraEvent()
    data object BorrarTodosLosProductos : ListaCompraEvent()
    data class UpdateProducto(val productoId: String, val nombre: String, val isImportant: Boolean = false) : ListaCompraEvent()
    data class ShowDialog(val show: Boolean) : ListaCompraEvent()
    data object ConfirmDelete : ListaCompraEvent()
    data object CancelDialog : ListaCompraEvent()
    data class StartEditingProduct(val productId: String, val currentName: String) : ListaCompraEvent()
    data class UpdateEditingText(val text: TextFieldValue) : ListaCompraEvent()
    data object SaveEditedProduct : ListaCompraEvent()
    data object CancelEditing : ListaCompraEvent()
    data object HideErrorAlert : ListaCompraEvent()
    data object HideSuccessAlert : ListaCompraEvent()
    data class ShowBottomSheet(val show: Boolean) : ListaCompraEvent()
    data class UpdateNewProductText(val text: TextFieldValue) : ListaCompraEvent()
    data object AddProducto : ListaCompraEvent()
    data object OnMenuClick: ListaCompraEvent()
    data object OnLogoutClick: ListaCompraEvent()
}