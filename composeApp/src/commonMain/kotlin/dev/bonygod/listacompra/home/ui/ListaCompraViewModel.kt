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
import dev.bonygod.listacompra.home.ui.model.ListaCompraUI
import dev.bonygod.listacompra.login.domain.usecase.AddSharedListUseCase
import dev.bonygod.listacompra.login.domain.usecase.DeleteAccountUseCase
import dev.bonygod.listacompra.login.domain.usecase.DeleteNotificationUseCase
import dev.bonygod.listacompra.login.domain.usecase.GetNotificationsUseCase
import dev.bonygod.listacompra.login.domain.usecase.GetUserUseCase
import dev.bonygod.listacompra.login.domain.usecase.LogOutUseCase
import dev.bonygod.listacompra.login.domain.usecase.ShareListaCompraUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.GetListasUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
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
    getNotificationsUseCase: GetNotificationsUseCase,
    private val shareListaCompraUseCase: ShareListaCompraUseCase,
    private val addSharedListUseCase: AddSharedListUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val getListasUseCase: GetListasUseCase
) : ViewModel() {
    private var notificationsJob: Job? = null
    private var productosJob: Job? = null
    private var lastResetRequestTime: Long = 0L
    private val _state = MutableStateFlow(ListaCompraState())
    val state: StateFlow<ListaCompraState> = _state

    private val _effect = MutableSharedFlow<ListaCompraEffect>(replay = 1)
    val effect: SharedFlow<ListaCompraEffect> = _effect.asSharedFlow()
    private val _currentListaId = MutableStateFlow<String?>(null)
    private val sharedProductosFlow = _currentListaId
        .flatMapLatest { listaId ->
            if (listaId != null) {
                getProductosUseCase(listaId)
                    .catch { emit(ListaCompraUI()) }
            } else {
                emptyFlow()
            }
        }
        .catch { emit(ListaCompraUI()) }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 0),
            replay = 1
        )
    private val sharedNotificationsFlow = getNotificationsUseCase()
        .catch { emit(emptyList()) }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 0),
            replay = 1
        )

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
        _currentListaId.value = null
    }

    fun loadUserData() {
        // Cancela los listeners activos
        stopNotificationsListener()
        // Reinicia los listeners y recarga los datos del usuario y productos
        viewModelScope.launch {
            try {
                getUserUseCase().fold(
                    onSuccess = { usuario ->
                        setState { setUser(usuario.toUI()) }
                        analyticsService.setUserId(usuario.uid)

                        // Actualiza el listaId para activar el flow compartido
                        _currentListaId.value = usuario.listas[0]

                        // Obtiene el nombre de la lista activa
                        getListasUseCase().fold(
                            onSuccess = { listas ->
                                val nombre = listas.firstOrNull()?.nombre ?: "Lista de la compra"
                                setState { setListaNombre(nombre) }
                            },
                            onFailure = { /* mantiene el nombre por defecto */ }
                        )

                        // Suscribirse al flow compartido de productos
                        productosJob = viewModelScope.launch {
                            sharedProductosFlow.collect { listaCompraUI ->
                                setState { getListaCompraUI(listaCompraUI) }
                            }
                        }

                        // Suscribirse al flow compartido de notificaciones
                        notificationsJob = viewModelScope.launch {
                            sharedNotificationsFlow.collect { notifications ->
                                setState { updateNotifications(notifications) }
                            }
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
                        sharedState.showLoading(false)
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                sharedState.showLoading(false)
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
            is ListaCompraEvent.DismissCustomDialog -> setState { showCustomDialog(false) }
            is ListaCompraEvent.ShareList -> shareList(event.email)
            is ListaCompraEvent.OnShareTextFieldChange -> setState { updateShareTextField(event.text) }
            is ListaCompraEvent.ShowNotificationsBottomSheet -> setState { showNotificationBottomSheet(event.show) }
            is ListaCompraEvent.OnAcceptSharedList -> acceptSharedList(event.listaId)
            is ListaCompraEvent.OnCancelSharedList -> cancelSharedList(event.listaId)
            is ListaCompraEvent.OnDeleteAccountClick -> setState { showDeleteAccountDialog(true) }
            is ListaCompraEvent.DismissDeleteAccountDialog -> setState { showDeleteAccountDialog(false) }
            is ListaCompraEvent.OnDeleteAccountConfirm -> deleteAccount()
            is ListaCompraEvent.TogglePurchased -> togglePurchased(event.productId)
            is ListaCompraEvent.OnMisListasClick -> navigator.navigateTo(Routes.MisListas)
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            // Parar listeners ANTES de borrar la cuenta
            stopNotificationsListener()
            deleteAccountUseCase()
            setState { showDeleteAccountDialog(false) }
            sharedState.showLoading(false)
            setState { ListaCompraState() }
            navigator.clearAndNavigateTo(Routes.Login)
        }
    }

    private fun cancelSharedList(listaId: String) {
        viewModelScope.launch {
            deleteNotificationUseCase(listaId)
            setState { showNotificationBottomSheet(false) }
        }
    }

    private fun acceptSharedList(listaId: String) {
        viewModelScope.launch {
            addSharedListUseCase(listaId).fold(
                onSuccess = {
                    deleteNotificationUseCase(listaId)
                    setState { showNotificationBottomSheet(false) }
                    // Recarga todos los datos del usuario para que los permisos de Firestore
                    // estén propagados antes de suscribirse a la nueva lista
                    loadUserData()
                },
                onFailure = { error ->
                    val errorMessage = (error as? Exception)?.message ?: "Error desconocido"
                    setState {
                        showErrorAlert(
                            "Error al aceptar la invitacion",
                            message = errorMessage
                        )
                    }
                }
            )
        }
    }

    private fun shareList(email: String) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        if (currentTime - lastResetRequestTime < 5.minutes.inWholeMilliseconds) {
            setEffect(ListaCompraEffect.ShowError("Debes esperar 5 minutos para volver a intentarlo."))
            return
        }
        val user = state.value.user
        viewModelScope.launch {
            shareListaCompraUseCase(user.nombre, user.listaId, email).fold(
                onSuccess = {
                    // Actualizar el timestamp después de compartir exitosamente
                    lastResetRequestTime = Clock.System.now().toEpochMilliseconds()
                    setState { showCustomDialog(false) }
                    setState {
                        showSuccessAlert(
                            "Lista compartida",
                            "La lista ha sido compartida con éxito."
                        )
                    }
                    setState { updateShareTextField(TextFieldValue("")) }
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
            // Parar listeners ANTES de cerrar sesión para evitar PERMISSION_DENIED
            stopNotificationsListener()
            logoutUseCase()
            sharedState.showLoading(false)
            setState { ListaCompraState() }
            navigator.clearAndNavigateTo(Routes.Login)
        }
    }

    private fun updateProducto(id: String, nombre: String, isImportant: Boolean) {
        viewModelScope.launch {
            try {
                val listaId = state.value.user.listaId
                val isPurchased = state.value.listaCompraUI.productos.find { it.id == id }?.isPurchased ?: false
                updateProductoUseCase(listaId, id, nombre, isImportant, isPurchased)
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
                    val isPurchased = originalProduct?.isPurchased ?: false
                    updateProductoUseCase(
                        listaId,
                        editingId,
                        editingText,
                        false,
                        isPurchased
                    )
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

    private fun togglePurchased(productId: String) {
        val producto = _state.value.listaCompraUI.productos.find { it.id == productId } ?: return
        val newIsPurchased = !producto.isPurchased
        setState { togglePurchased(productId) }
        viewModelScope.launch {
            try {
                val listaId = state.value.user.listaId
                updateProductoUseCase(
                    listaId,
                    productId,
                    producto.nombre,
                    producto.isImportant,
                    newIsPurchased
                )
            } catch (e: Exception) {
                e.printStackTrace()
                setState { togglePurchased(productId) }
                setState {
                    showErrorAlert(
                        "Error al actualizar",
                        "No se pudo guardar el estado del producto. Verifica tu conexión e inténtalo de nuevo."
                    )
                }
            }
        }
    }

    private fun borrarTodosLosProductos() {        viewModelScope.launch {
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