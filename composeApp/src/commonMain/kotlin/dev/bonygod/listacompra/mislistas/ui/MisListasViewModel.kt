package dev.bonygod.listacompra.mislistas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bonygod.listacompra.core.navigation.Navigator
import dev.bonygod.listacompra.core.navigation.Routes
import dev.bonygod.listacompra.login.domain.usecase.GetUserUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.AddNewListaUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.GetListasUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.RenameListaUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.SetDefaultListaUseCase
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
    private val addNewListaUseCase: AddNewListaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MisListasState())
    val state: StateFlow<MisListasState> = _state

    private var userId: String = ""

    fun loadListas() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            getUserUseCase().fold(
                onSuccess = { user ->
                    userId = user.uid
                    getListasUseCase().fold(
                        onSuccess = { listas ->
                            _state.value = _state.value.copy(
                                listas = listas.map {
                                    ListaInfoUI(id = it.id, nombre = it.nombre, isDefault = it.isDefault)
                                },
                                isLoading = false
                            )
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

