package dev.bonygod.listacompra.notificaciones.ui.composables.interactions

sealed class NotificacionesEvent {
    data object GoBack : NotificacionesEvent()
    data class Accept(val listaId: String, val listaNombre: String) : NotificacionesEvent()
    data class Reject(val listaId: String) : NotificacionesEvent()
}
