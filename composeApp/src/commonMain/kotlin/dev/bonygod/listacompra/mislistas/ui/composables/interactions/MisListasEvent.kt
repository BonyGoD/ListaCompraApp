package dev.bonygod.listacompra.mislistas.ui.composables.interactions

sealed class MisListasEvent {
    data class SelectLista(val listaId: String) : MisListasEvent()
    data object GoBack : MisListasEvent()
    data class ShowRenameDialog(val listaId: String, val currentNombre: String) : MisListasEvent()
    data class ConfirmRename(val listaId: String, val nombre: String) : MisListasEvent()
    data object ShowCreateDialog : MisListasEvent()
    data class ConfirmCreate(val nombre: String) : MisListasEvent()
    data object DismissDialog : MisListasEvent()

    /** Elegir a qué lista escribe Alexa. Distinto de SelectLista: no cambia la
     *  predeterminada ni navega, solo actualiza `listaAlexa`. */
    data class SelectListaAlexa(val listaId: String) : MisListasEvent()

    /** Usuario anónimo pulsando "vincular cuenta" en la sección Alexa. */
    data object OnLinkAccountForAlexaClick : MisListasEvent()
}
