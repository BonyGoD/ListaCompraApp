package dev.bonygod.listacompra.ui

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bonygod.listacompra.domain.usecase.AddProductoUseCase
import dev.bonygod.listacompra.domain.usecase.DeleteAllProductosUseCase
import dev.bonygod.listacompra.domain.usecase.DeleteProductoUseCase
import dev.bonygod.listacompra.domain.usecase.GetProductosUseCase
import dev.bonygod.listacompra.domain.usecase.UpdateProductoUseCase
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.ui.composables.preview.ListaCompraPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListaCompraViewModel(
    private val getProductosUseCase: GetProductosUseCase,
    private val deleteProductoUseCase: DeleteProductoUseCase,
    private val deleteAllProductosUseCase: DeleteAllProductosUseCase,
    private val updateProductoUseCase: UpdateProductoUseCase,
    private val addProductoUseCase: AddProductoUseCase
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
            is ListaCompraEvent.UpdateProducto -> updateProducto(
                event.productoId,
                event.nombre,
                event.isImportant
            )

            is ListaCompraEvent.StartEditingProduct -> setState {
                startEditingProduct(
                    event.productId,
                    event.currentName
                )
            }

            is ListaCompraEvent.UpdateEditingText -> setState { updateEditingText(event.text) }
            is ListaCompraEvent.SaveEditedProduct -> saveEditedProduct()
            is ListaCompraEvent.CancelEditing -> setState { cancelEditing() }
            is ListaCompraEvent.HideErrorAlert -> setState { hideErrorAlert() }
            is ListaCompraEvent.HideSuccessAlert -> setState { hideSuccessAlert() }
            is ListaCompraEvent.ShowBottomSheet -> setState { showBottomSheet(event.show) }
            is ListaCompraEvent.UpdateNewProductText -> setState { updateNewProductText(event.text) }
            is ListaCompraEvent.AddProducto -> addProducto()
        }
    }

    private fun updateProducto(id: String, nombre: String, isImportant: Boolean = false) {
        viewModelScope.launch {
            try {
                updateProductoUseCase(id, nombre, isImportant)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveEditedProduct() {
        val currentState = _state.value
        val editingId = currentState.editingProductId
        val editingText = currentState.editingText.text

        if (editingId != null && editingText.isNotBlank()) {
            val originalProduct = currentState.listaCompraUI.productos.find { it.id == editingId }

            setState { saveEditedProduct() }

            viewModelScope.launch {
                try {
                    updateProductoUseCase(
                        editingId,
                        editingText,
                        false
                    ) // isImportant = false por defecto
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (originalProduct != null) {
                        setState {
                            startEditingProduct(editingId, originalProduct.nombre)
                        }
                    } else {
                        setState { cancelEditing() }
                    }

                    setState {
                        showErrorAlert(
                            "Error al actualizar",
                            "No se pudo actualizar el producto. Verifica tu conexión a internet e inténtalo nuevamente."
                        )
                    }
                }
            }
        } else {
            setState { cancelEditing() }
        }
    }

    private fun addProducto() {
        val currentState = _state.value
        val newProductText = currentState.newProductText.text.trim()

        if (newProductText.isNotBlank()) {
            viewModelScope.launch {
                try {
                    addProductoUseCase(newProductText)
                    setState {
                        copy(
                            showBottomSheet = false,
                            newProductText = TextFieldValue("")
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    setState {
                        showErrorAlert(
                            "Error al agregar producto",
                            "No se pudo agregar el producto. Verifica tu conexión a internet e inténtalo nuevamente."
                        )
                    }
                }
            }
        }
    }

    private fun borrarProducto(id: String) {
        setState { showLoading(true) }
        viewModelScope.launch {
            try {
                deleteProductoUseCase(id)
                setState { removeProducto(id) }
                setState { showLoading(false) }
            } catch (e: Exception) {
                e.printStackTrace()
                setState { showLoading(false) }
                setState {
                    showErrorAlert(
                        "Error al eliminar",
                        "No se pudo eliminar el producto. Verifica tu conexión a internet e inténtalo nuevamente."
                    )
                }
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
                setState {
                    showErrorAlert(
                        "Error al eliminar lista",
                        "No se pudo eliminar la lista completa. Verifica tu conexión a internet e inténtalo nuevamente."
                    )
                }
            }
        }
    }
}