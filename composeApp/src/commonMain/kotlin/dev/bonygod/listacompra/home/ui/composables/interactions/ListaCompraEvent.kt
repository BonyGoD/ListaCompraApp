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
    data class UpdateNewProductText(val text: TextFieldValue) : ListaCompraEvent()
    data object AddProducto : ListaCompraEvent()
    data object OnMenuClick : ListaCompraEvent()
    data object OnLogoutClick : ListaCompraEvent()
    data object OnShareListClick : ListaCompraEvent()
    data class ShareList(val email: String) : ListaCompraEvent()
    data object DismissCustomDialog : ListaCompraEvent()
    data object DismissDeleteAccountDialog : ListaCompraEvent()
    data class OnShareTextFieldChange(val text: TextFieldValue) : ListaCompraEvent()
    data object OnDeleteAccountClick: ListaCompraEvent()
    data object OnDeleteAccountConfirm: ListaCompraEvent()
    data class TogglePurchased(val productId: String) : ListaCompraEvent()
    data object OnMisListasClick : ListaCompraEvent()
    data object OnAlexaClick : ListaCompraEvent()

    data object OnNotificacionesClick : ListaCompraEvent()

    data class OnNotificationsPermissionResult(val granted: Boolean) : ListaCompraEvent()
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
    data object OnEditNombreClick : ListaCompraEvent()
    data class ConfirmEditNombre(val nombre: String) : ListaCompraEvent()
    data object DismissEditNombreDialog : ListaCompraEvent()

    /** "Sí, avisadme" en el diálogo propio: marca el aviso como ofrecido y lanza el
     *  diálogo del sistema (resultado por OnNotificationsPermissionResult). */
    data object OnNotificationsOfferAccept : ListaCompraEvent()

    /** "Ahora no": marca el aviso como ofrecido sin llegar a pedir el permiso. */
    data object OnNotificationsOfferDecline : ListaCompraEvent()

    /** El permiso se pidió desde la entrada del menú lateral: si ya pasó por ahí,
     *  el diálogo propio de Home no tiene sentido y no debe volver a ofrecerse. */
    data object OnNotificationsPermissionRequestedFromMenu : ListaCompraEvent()

    /** La entrada del menú, cuando ya están activadas: no hace falta volver a pedir el
     *  permiso, solo enseñar el diálogo de estado (modo "activadas"). */
    data object OnNotificationsStatusClick : ListaCompraEvent()

    /** Cerrar el diálogo de estado de notificaciones sin hacer nada más. */
    data object OnCloseNotificationsStatusDialog : ListaCompraEvent()

    /** "Abrir ajustes" en el diálogo de estado: aquí solo se cierra el diálogo. Abrir los
     *  ajustes de verdad lo hace HomeScreen en el mismo lambda que dispara este evento,
     *  sin pasar por el canal de efectos (ver comentario en HomeScreen). */
    data object OnOpenNotificationsSettingsClick : ListaCompraEvent()

    /** Abre el diálogo de vinculación de cuenta directamente, sin pasar por el aviso
     *  de "compartir requiere cuenta". Lo usa la pantalla Alexa, que deja la petición
     *  en `PendingHomeAction` antes de navegar a Home. */
    data object OnOpenLinkAccountDialog : ListaCompraEvent()
}