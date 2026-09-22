package dev.bonygod.listacompra.notificaciones.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bonygod.listacompra.core.navigation.Navigator
import dev.bonygod.listacompra.core.navigation.PendingHomeAction
import dev.bonygod.listacompra.login.domain.usecase.AddSharedListUseCase
import dev.bonygod.listacompra.login.domain.usecase.DeleteNotificationUseCase
import dev.bonygod.listacompra.login.domain.usecase.GetNotificationsUseCase
import dev.bonygod.listacompra.notificaciones.ui.composables.interactions.NotificacionesEvent
import dev.bonygod.listacompra.notificaciones.ui.composables.interactions.NotificacionesState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class NotificacionesViewModel(
    private val navigator: Navigator,
    private val pendingHomeAction: PendingHomeAction,
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val addSharedListUseCase: AddSharedListUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NotificacionesState())
    val state: StateFlow<NotificacionesState> = _state

    private var notificationsJob: Job? = null

    fun startListening() {
        if (notificationsJob != null) return
        notificationsJob = viewModelScope.launch {
            getNotificationsUseCase()
                .catch { emit(emptyList()) }
                .collect { notifications ->
                    _state.value = _state.value.copy(notifications = notifications, isLoading = false)
                }
        }
    }

    fun onEvent(event: NotificacionesEvent) {
        when (event) {
            is NotificacionesEvent.GoBack -> navigator.goBack()
            is NotificacionesEvent.Accept -> accept(event.listaId, event.listaNombre)
            is NotificacionesEvent.Reject -> reject(event.listaId)
        }
    }

    private fun accept(listaId: String, listaNombre: String) {
        viewModelScope.launch {
            addSharedListUseCase(listaId, listaNombre).fold(
                onSuccess = {
                    deleteNotificationUseCase(listaId)
                    pendingHomeAction.requestRecargarHome()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(error = e.message ?: "Error al aceptar la invitación")
                }
            )
        }
    }

    private fun reject(listaId: String) {
        viewModelScope.launch { deleteNotificationUseCase(listaId) }
    }
}
