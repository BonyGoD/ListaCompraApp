package dev.bonygod.listacompra.mislistas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bonygod.listacompra.core.navigation.Navigator
import dev.bonygod.listacompra.core.navigation.Routes
import dev.bonygod.listacompra.login.domain.usecase.GetUserUseCase
import dev.bonygod.listacompra.login.domain.usecase.IsAnonymousUserUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.AddNewListaUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.GetAlexaConfigUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.GetListasUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.RenameListaUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.SetDefaultListaUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.SetListaAlexaUseCase
import dev.bonygod.listacompra.mislistas.ui.composables.interactions.MisListasEvent
import dev.bonygod.listacompra.mislistas.ui.composables.interactions.MisListasState
import dev.bonygod.listacompra.mislistas.ui.model.ListaInfoUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MisListasViewModel(
    private val navigator: Navigator,
    private val getListasUseCase: GetListasUseCase,
    private val setDefaultListaUseCase: SetDefaultListaUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val renameListaUseCase: RenameListaUseCase,
    private val addNewListaUseCase: AddNewListaUseCase,
    private val isAnonymousUserUseCase: IsAnonymousUserUseCase,
    private val getAlexaConfigUseCase: GetAlexaConfigUseCase,
    private val setListaAlexaUseCase: SetListaAlexaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MisListasState())
    val state: StateFlow<MisListasState> = _state

    private var userId: String = ""

    fun loadListas() {
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

    fun onEvent(event: MisListasEvent) {
        when (event) {
            is MisListasEvent.SelectLista -> selectLista(event.listaId)
            is MisListasEvent.GoBack -> navigator.goBack()
            is MisListasEvent.ShowRenameDialog -> _state.value = _state.value.copy(
                renameDialogListaId = event.listaId,
                renameDialogCurrentNombre = event.currentNombre
            )
            is MisListasEvent.ConfirmRename -> renameLista(event.listaId, event.nombre)
            is MisListasEvent.ShowCreateDialog -> _state.value = _state.value.copy(showCreateDialog = true)
            is MisListasEvent.ConfirmCreate -> createLista(event.nombre)
            is MisListasEvent.DismissDialog -> _state.value = _state.value.copy(
                renameDialogListaId = null,
                renameDialogCurrentNombre = "",
                showCreateDialog = false
            )
            is MisListasEvent.SelectListaAlexa -> selectListaAlexa(event.listaId)
            is MisListasEvent.OnLinkAccountForAlexaClick ->
                navigator.clearAndNavigateTo(Routes.Home(userId, openLinkAccount = true))
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

    private fun selectLista(listaId: String) {
        viewModelScope.launch {
            setDefaultListaUseCase(listaId).fold(
                onSuccess = { navigator.clearAndNavigateTo(Routes.Home(userId)) },
                onFailure = { e ->
                    _state.value = _state.value.copy(error = e.message ?: "Error al seleccionar la lista")
                }
            )
        }
    }


    private fun renameLista(listaId: String, nombre: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                renameDialogListaId = null,
                renameDialogCurrentNombre = ""
            )
            renameListaUseCase(listaId, nombre).fold(
                onSuccess = { loadListas() },
                onFailure = { e ->
                    _state.value = _state.value.copy(error = e.message ?: "Error al renombrar la lista")
                }
            )
        }
    }

    private fun createLista(nombre: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(showCreateDialog = false)
            addNewListaUseCase(nombre).fold(
                onSuccess = { loadListas() },
                onFailure = { e ->
                    _state.value = _state.value.copy(error = e.message ?: "Error al crear la lista")
                }
            )
        }
    }
}

