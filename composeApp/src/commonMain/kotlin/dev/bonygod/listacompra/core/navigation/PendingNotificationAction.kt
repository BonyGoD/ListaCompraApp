package dev.bonygod.listacompra.core.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class PendingNotificationAction {

    var notificacionesRequested by mutableStateOf(false)
        private set

    fun requestNotificaciones() {
        notificacionesRequested = true
    }

    fun consumeNotificaciones(): Boolean {
        val requested = notificacionesRequested
        notificacionesRequested = false
        return requested
    }
}
