package dev.bonygod.listacompra.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bonygod.listacompra.domain.usecase.GetProductosUseCase
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.ui.composables.preview.ListaCompraPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListaCompraViewModel(
    private val getProductosUseCase: GetProductosUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ListaCompraState())
    val state: StateFlow<ListaCompraState> = _state

    fun setState(reducer: ListaCompraState.() -> ListaCompraState) {
        _state.value = _state.value.reducer()
    }

    init {
        viewModelScope.launch {
            try {
                getProductosUseCase().collect { listaCompraUI ->
                    setState { getListaCompraUI(listaCompraUI) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createInitialState() {
        setState { ListaCompraState(listaCompraUI = ListaCompraPreview.ListaCompraUI) }
    }

    //TODO: Crear eventos
    fun onEvent(event: ListaCompraEvent) {
        when (event) {
            is ListaCompraEvent.ActualizarProductos -> cargarNuevosProductos()
            // Manejar otros eventos...
        }
    }

    //TODO: Crear eventos
    private fun cargarNuevosProductos() {
        setState { showLoading(true) }
        // Simulación de llamada al servicio (debes reemplazar por tu lógica real)
        // Por ejemplo, podrías usar coroutines para la llamada asíncrona
        // servicio.obtenerProductos { nuevosProductos ->
        //     setState { updateListaCompra(ListaCompraUI(productos = nuevosProductos)) }
        //     setState { showLoading(false) }
        // }
    }
}