package dev.bonygod.listacompra.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bonygod.listacompra.domain.usecase.DeleteAllProductosUseCase
import dev.bonygod.listacompra.domain.usecase.DeleteProductoUseCase
import dev.bonygod.listacompra.domain.usecase.GetProductosUseCase
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.ui.composables.preview.ListaCompraPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListaCompraViewModel(
    private val getProductosUseCase: GetProductosUseCase,
    private val deleteProductoUseCase: DeleteProductoUseCase,
    private val deleteAllProductosUseCase: DeleteAllProductosUseCase
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

    fun onEvent(event: ListaCompraEvent) {
        when (event) {
            is ListaCompraEvent.BorrarProducto -> borrarProducto(event.productId)
            is ListaCompraEvent.BorrarTodosLosProductos -> borrarTodosLosProductos()
            is ListaCompraEvent.ShowDialog -> setState { showDialog(event.show) }
            is ListaCompraEvent.ConfirmDelete -> {
                borrarTodosLosProductos()
                setState { showDialog(false) }
            }
            is ListaCompraEvent.CancelDialog -> setState { showDialog(false) }
        }
    }

    private fun borrarProducto(id: String) {
        setState { showLoading(true) }
        viewModelScope.launch {
            try {
                deleteProductoUseCase.invoke(id)
                setState { removeProducto(id) }
                setState { showLoading(false) }
            } catch (e: Exception) {
                e.printStackTrace()
                setState { showLoading(false) }
            }
        }
    }

    private fun borrarTodosLosProductos() {
        viewModelScope.launch {
            try {
                deleteAllProductosUseCase.invoke()
                setState { clearAllProductos() }
                setState { showLoading(false) }
            } catch (e: Exception) {
                e.printStackTrace()
                setState { showLoading(false) }
            }
        }
    }
}