package dev.bonygod.listacompra.alexa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bonygod.listacompra.alexa.ui.composables.interactions.AlexaEvent
import dev.bonygod.listacompra.alexa.ui.composables.interactions.AlexaState
import dev.bonygod.listacompra.core.navigation.Navigator
import dev.bonygod.listacompra.core.navigation.PendingHomeAction
import dev.bonygod.listacompra.core.navigation.Routes
import dev.bonygod.listacompra.login.domain.usecase.GetUserUseCase
import dev.bonygod.listacompra.login.domain.usecase.IsAnonymousUserUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.GetAlexaConfigUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.GetListasUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.SetListaAlexaUseCase
import dev.bonygod.listacompra.mislistas.ui.model.ListaInfoUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Pantalla propia de Alexa (fase 5 del plan, reestructurada): antes era una sección
 * dentro de MisListasViewModel. Mismo comportamiento, solo separado porque la
 * configuración de Alexa no es una propiedad de las listas.
 */
class AlexaViewModel(
    private val navigator: Navigator,
    private val pendingHomeAction: PendingHomeAction,
    private val getListasUseCase: GetListasUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val isAnonymousUserUseCase: IsAnonymousUserUseCase,
    private val getAlexaConfigUseCase: GetAlexaConfigUseCase,
    private val setListaAlexaUseCase: SetListaAlexaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AlexaState())
    val state: StateFlow<AlexaState> = _state

    private var userId: String = ""

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val anonymous = isAnonymousUserUseCase()
            getUserUseCase().fold(
                onSuccess = { user ->
                    userId = user.uid
                    getListasUseCase().fold(
                        onSuccess = { listas ->
                            _state.value = _state.value.copy(
                                listas = listas.map {
                                    ListaInfoUI(id = it.id, nombre = it.nombre, isDefault = it.isDefault)
                                },
                                isLoading = false,
                                isAnonymous = anonymous
                            )
                            // Los anónimos no pueden vincular Alexa (no hay sesión que
                            // reproducir en la página de authorize), así que no hace
                            // falta ni leer su configuración.
                            if (!anonymous) loadAlexaConfig()
                        },
                        onFailure = { e ->
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = e.message ?: "Error al cargar las listas"
                            )
                        }
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al obtener el usuario"
                    )
                }
            )
        }
    }

    private fun loadAlexaConfig() {
        viewModelScope.launch {
            getAlexaConfigUseCase().fold(
                onSuccess = { config ->
                    _state.value = _state.value.copy(
                        alexaVinculada = config.alexaVinculada,
                        listaAlexa = config.listaAlexa
                    )
                },
                onFailure = {
                    // No bloquea el resto de la pantalla: las listas ya están cargadas.
                }
            )
        }
    }

    fun onEvent(event: AlexaEvent) {
        when (event) {
            is AlexaEvent.GoBack -> navigator.goBack()
            is AlexaEvent.SelectListaAlexa -> selectListaAlexa(event.listaId)
            is AlexaEvent.OnLinkAccountForAlexaClick -> {
                pendingHomeAction.requestLinkAccount()
                navigator.clearAndNavigateTo(Routes.Home(userId))
            }
        }
    }

    private fun selectListaAlexa(listaId: String) {
        viewModelScope.launch {
            setListaAlexaUseCase(listaId).fold(
                onSuccess = { _state.value = _state.value.copy(listaAlexa = listaId) },
                onFailure = { e ->
                    _state.value = _state.value.copy(error = e.message ?: "Error al cambiar la lista de Alexa")
                }
            )
        }
    }
}
