package dev.bonygod.listacompra.home.ui.composables.interactions

import androidx.compose.ui.text.input.TextFieldValue

sealed class ListaCompraEvent {
    data class BorrarProducto(val productId: String) : ListaCompraEvent()
    data object BorrarTodosLosProductos : ListaCompraEvent()
    data class UpdateProducto(
        val productoId: String,
        val nombre: String,
        val isImportant: Boolean = false
    ) : ListaCompraEvent()

    data class ShowDialog(val show: Boolean) : ListaCompraEvent()
    data object ConfirmDelete : ListaCompraEvent()
    data object CancelDialog : ListaCompraEvent()
    data class StartEditingProduct(val productId: String, val currentName: String) :
        ListaCompraEvent()

    data class UpdateEditingText(val text: TextFieldValue) : ListaCompraEvent()
    data object SaveEditedProduct : ListaCompraEvent()
    data object CancelEditing : ListaCompraEvent()
    data object HideErrorAlert : ListaCompraEvent()
    data object HideSuccessAlert : ListaCompraEvent()
    data class ShowBottomSheet(val show: Boolean) : ListaCompraEvent()
    data class ShowNotificationsBottomSheet(val show: Boolean) : ListaCompraEvent()
    data class UpdateNewProductText(val text: TextFieldValue) : ListaCompraEvent()
    data object AddProducto : ListaCompraEvent()
    data object OnMenuClick : ListaCompraEvent()
    data object OnLogoutClick : ListaCompraEvent()
    data object OnShareListClick : ListaCompraEvent()
    data class ShareList(val email: String) : ListaCompraEvent()
    data object DismissCustomDialog : ListaCompraEvent()
    data object DismissDeleteAccountDialog : ListaCompraEvent()
    data class OnShareTextFieldChange(val text: TextFieldValue) : ListaCompraEvent()
    data class OnAcceptSharedList(
        val listaId: String,
        val listaNombre: String
    ) : ListaCompraEvent()
    data class OnCancelSharedList(val listaId: String) : ListaCompraEvent()
    data object OnDeleteAccountClick: ListaCompraEvent()
    data object OnDeleteAccountConfirm: ListaCompraEvent()
    data class TogglePurchased(val productId: String) : ListaCompraEvent()
    data object OnMisListasClick : ListaCompraEvent()
    data object OnForceCrashClick : ListaCompraEvent()
    data object OnForceNonFatalClick : ListaCompraEvent()
    data object OnLoginFromMenuClick : ListaCompraEvent()
    data object OnConfirmLoginDataLoss : ListaCompraEvent()
    data object OnCancelLoginDataLoss : ListaCompraEvent()
    data object OnLoginFromEmptyListClick : ListaCompraEvent()
    data object OnShareAccountRequiredConfirm : ListaCompraEvent()
    data object OnShareAccountRequiredCancel : ListaCompraEvent()
    data class OnLinkEmailChange(val text: TextFieldValue) : ListaCompraEvent()
    data class OnLinkPasswordChange(val text: TextFieldValue) : ListaCompraEvent()
    data object OnLinkAccountConfirm : ListaCompraEvent()
    data object OnDismissLinkAccountDialog : ListaCompraEvent()
    data object OnConfirmLinkCredentialInUse : ListaCompraEvent()
    data object OnCancelLinkCredentialInUse : ListaCompraEvent()
}