package dev.bonygod.listacompra.ui

import androidx.lifecycle.ViewModel
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.ui.composables.preview.ListaCompraPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ListaCompraViewModel : ViewModel() {
    private val _state = MutableStateFlow(ListaCompraState())
    val state: StateFlow<ListaCompraState> = _state

    fun setState(reducer: ListaCompraState.() -> ListaCompraState) {
        _state.value = _state.value.reducer()
    }

    fun createInitialState() { setState { ListaCompraState(listaCompraUI = ListaCompraPreview.ListaCompraUI) } }

    fun onEvent(event: ListaCompraEvent) {
        when (event) {
            is ListaCompraEvent.ActualizarProductos -> cargarNuevosProductos()
            // Manejar otros eventos...
        }
    }

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