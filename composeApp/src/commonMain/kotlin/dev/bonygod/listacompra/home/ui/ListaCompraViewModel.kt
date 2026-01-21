package dev.bonygod.listacompra.home.ui

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bonygod.listacompra.common.ui.state.SharedState
import dev.bonygod.listacompra.core.analytics.AnalyticsService
import dev.bonygod.listacompra.core.navigation.Navigator
import dev.bonygod.listacompra.core.navigation.Routes
import dev.bonygod.listacompra.home.domain.usecase.AddProductoUseCase
import dev.bonygod.listacompra.home.domain.usecase.DeleteAllProductosUseCase
import dev.bonygod.listacompra.home.domain.usecase.DeleteProductoUseCase
import dev.bonygod.listacompra.home.domain.usecase.GetProductosUseCase
import dev.bonygod.listacompra.home.domain.usecase.UpdateProductoUseCase
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEffect
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.home.ui.mapper.toUI
import dev.bonygod.listacompra.login.domain.usecase.GetNotificationsUseCase
import dev.bonygod.listacompra.login.domain.usecase.GetUserUseCase
import dev.bonygod.listacompra.login.domain.usecase.LogOutUseCase
import dev.bonygod.listacompra.login.domain.usecase.ShareListaCompraUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ListaCompraViewModel(
    private val navigator: Navigator,
    private val sharedState: SharedState,
    private val getProductosUseCase: GetProductosUseCase,
    private val deleteProductoUseCase: DeleteProductoUseCase,
    private val deleteAllProductosUseCase: DeleteAllProductosUseCase,
    private val updateProductoUseCase: UpdateProductoUseCase,
    private val addProductoUseCase: AddProductoUseCase,
    private val analyticsService: AnalyticsService,
    private val getUserUseCase: GetUserUseCase,
    private val logoutUseCase: LogOutUseCase,
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val shareListaCompraUseCase: ShareListaCompraUseCase
) : ViewModel() {
    private var notificationsJob: Job? = null
    private var productosJob: Job? = null
    private val _state = MutableStateFlow(ListaCompraState())
    val state: StateFlow<ListaCompraState> = _state

    private val _effect = MutableSharedFlow<ListaCompraEffect>(replay = 1)
    val effect: SharedFlow<ListaCompraEffect> = _effect.asSharedFlow()

    fun setState(reducer: ListaCompraState.() -> ListaCompraState) {
        _state.value = _state.value.reducer()
    }

    private fun setEffect(effect: ListaCompraEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    fun stopNotificationsListener() {
        notificationsJob?.cancel()
        productosJob?.cancel()
        notificationsJob = null
        productosJob = null
    }

    init {
        analyticsService.logScreenView("lista_compra_screen")
        productosJob?.cancel()
        productosJob = viewModelScope.launch {
            try {
                getUserUseCase().fold(
                    onSuccess = { usuario ->
                        setState { setUser(usuario.toUI()) }
                        analyticsService.setUserId(usuario.uid)
                        getProductosUseCase(usuario.listas[0]).collect { listaCompraUI ->
                            setState { getListaCompraUI(listaCompraUI) }
                        }
                        sharedState.showLoading(false)
                    },
                    onFailure = { error ->
                        val errorMessage = (error as? Exception)?.message ?: "Error desconocido"
                        setState {
                            showErrorAlert(
                                "Error al obtener el Usuario",
                                message = errorMessage
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        notificationsJob?.cancel()
        notificationsJob = viewModelScope.launch {
            getNotificationsUseCase().collect { notifications ->
                setState { updateNotifications(notifications) }
            }
        }
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
            is ListaCompraEvent.OnMenuClick -> setState { showMenu() }
            is ListaCompraEvent.OnLogoutClick -> logOut()
            is ListaCompraEvent.OnShareListClick -> setState { showCustomDialog(true) }
            is ListaCompraEvent.DismissCustomDialog -> setState { showDialog(false) }
            is ListaCompraEvent.ShareList -> shareList(event.email)
            is ListaCompraEvent.OnShareTextFieldChange -> setState { updateShareTextField(event.text) }
            is ListaCompraEvent.OnNotificationClick -> setState { updateShowNotificationDialog(true) }
        }
    }

    private fun shareList(email: String) {
        val user = state.value.user
        viewModelScope.launch {
            shareListaCompraUseCase(user.nombre, user.listaId, email).fold(
                onSuccess = {
                    setState { showCustomDialog(false) }
                    setState {
                        showSuccessAlert(
                            "Lista compartida",
                            "La lista ha sido compartida con éxito."
                        )
                    }
                },
                onFailure = { error ->
                    val errorMessage = (error as? Exception)?.message ?: "Error desconocido"
                    setState {
                        showErrorAlert(
                            "Error al compartir",
                            message = errorMessage
                        )
                    }
                }
            )
        }
    }

    private fun logOut() {
        viewModelScope.launch {
            logoutUseCase()
            sharedState.showLoading(false)
            stopNotificationsListener()
            navigator.clearAndNavigateTo(Routes.Login)
        }
    }

    private fun updateProducto(id: String, nombre: String, isImportant: Boolean = false) {
        viewModelScope.launch {
            try {
                val listaId = state.value.user.listaId
                updateProductoUseCase(listaId, id, nombre, isImportant)
                analyticsService.logProductoUpdated(nombre, isImportant)
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
                    val listaId = state.value.user.listaId
                    updateProductoUseCase(
                        listaId,
                        editingId,
                        editingText,
                        false
                    ) // isImportant = false por defecto
                    analyticsService.logProductoUpdated(editingText, false)
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
                    val listaId = state.value.user.listaId
                    addProductoUseCase(listaId, newProductText)
                    analyticsService.logProductoAdded(newProductText)
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
        viewModelScope.launch {
            try {
                val listaId = state.value.user.listaId
                // Obtener el nombre del producto antes de borrarlo
                val producto = _state.value.listaCompraUI.productos.find { it.id == id }
                deleteProductoUseCase(listaId, id)
                if (producto != null) {
                    analyticsService.logProductoDeleted(producto.nombre)
                }
                setState { removeProducto(id) }
            } catch (e: Exception) {
                e.printStackTrace()
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
                val listaId = state.value.user.listaId
                val totalProductos = _state.value.listaCompraUI.productos.size
                deleteAllProductosUseCase.invoke(listaId)
                analyticsService.logListaCleared(totalProductos)
                setState { clearAllProductos() }
            } catch (e: Exception) {
                e.printStackTrace()
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