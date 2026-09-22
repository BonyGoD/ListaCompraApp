package dev.bonygod.listacompra.notificaciones.ui.composables.interactions

import dev.bonygod.listacompra.login.ui.composables.model.NotificationsUI

data class NotificacionesState(
    val notifications: List<NotificationsUI> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
