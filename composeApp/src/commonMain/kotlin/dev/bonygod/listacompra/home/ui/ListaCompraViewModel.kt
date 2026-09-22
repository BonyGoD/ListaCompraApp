package dev.bonygod.listacompra.home.ui

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bonygod.crashlytics.kmp.core.CrashReporter
import dev.bonygod.listacompra.common.ui.state.SharedState
import dev.bonygod.listacompra.core.CustomFailures.LoginFailure
import dev.bonygod.listacompra.core.analytics.AnalyticsService
import dev.bonygod.listacompra.core.navigation.Navigator
import dev.bonygod.listacompra.core.navigation.Routes
import dev.bonygod.listacompra.core.preferences.PreferenciasLocales
import dev.bonygod.listacompra.home.domain.usecase.AddProductoUseCase
import dev.bonygod.listacompra.home.domain.usecase.DeleteAllProductosUseCase
import dev.bonygod.listacompra.home.domain.usecase.DeleteProductoUseCase
import dev.bonygod.listacompra.home.domain.usecase.GetProductosUseCase
import dev.bonygod.listacompra.home.domain.usecase.UpdateProductoUseCase
import dev.bonygod.listacompra.home.ui.composables.interactions.LinkAccountOrigin
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEffect
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.home.ui.mapper.toUI
import dev.bonygod.listacompra.home.ui.model.ListaCompraUI
import dev.bonygod.listacompra.login.domain.usecase.DeleteAccountUseCase
import dev.bonygod.listacompra.login.domain.usecase.GetNotificationsUseCase
import dev.bonygod.listacompra.login.domain.usecase.GetUserUseCase
import dev.bonygod.listacompra.login.domain.usecase.GuardarTokenPushUseCase
import dev.bonygod.listacompra.login.domain.usecase.IsAnonymousUserUseCase
import dev.bonygod.listacompra.login.domain.usecase.LinkAccountWithEmailUseCase
import dev.bonygod.listacompra.login.domain.usecase.LogOutUseCase
import dev.bonygod.listacompra.login.domain.usecase.ShareListaCompraUseCase
import dev.bonygod.listacompra.login.domain.usecase.UpdateNombreUseCase
import dev.bonygod.listacompra.login.domain.usecase.UserLoginUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.GetListasUseCase
import dev.bonygod.listacompra.notificaciones.PushNotifications
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
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.home_alert_error_add_product_title
import listacompra.composeapp.generated.resources.home_alert_error_delete_account_title
import listacompra.composeapp.generated.resources.home_alert_error_delete_list_title
import listacompra.composeapp.generated.resources.home_alert_error_delete_product_title
import listacompra.composeapp.generated.resources.home_alert_error_get_user_title
import listacompra.composeapp.generated.resources.home_alert_error_link_account_title
import listacompra.composeapp.generated.resources.home_alert_error_login_title
import listacompra.composeapp.generated.resources.home_alert_error_share_title
import listacompra.composeapp.generated.resources.home_alert_error_update_title
import listacompra.composeapp.generated.resources.home_alert_share_rate_limit_message
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/** Clave en PreferenciasLocales: por dispositivo, no por cuenta, porque el permiso de
 *  notificaciones también lo es. Se marca tanto desde el diálogo propio como desde la
 *  entrada del menú lateral, para no ofrecerlo dos veces. */
private const val NOTIFICATIONS_OFFER_KEY = "notifications_permission_offered"

/** Cuándo se envió la última invitación. En preferencias y no en memoria: era un campo
 *  del ViewModel, así que bastaba con cerrar y reabrir la app para saltarse la espera.
 *  El servidor aplica su propio límite, más corto, para quien llame al endpoint por
 *  fuera de la app. */
private const val ULTIMA_INVITACION_KEY = "ultima_invitacion_ms"
private val INTERVALO_ENTRE_INVITACIONES = 2.minutes

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
    private val isAnonymousUserUseCase: IsAnonymousUserUseCase,
    private val logoutUseCase: LogOutUseCase,
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val shareListaCompraUseCase: ShareListaCompraUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val getListasUseCase: GetListasUseCase,
    private val linkAccountWithEmailUseCase: LinkAccountWithEmailUseCase,
    private val userLoginUseCase: UserLoginUseCase,
    private val updateNombreUseCase: UpdateNombreUseCase,
    private val guardarTokenPushUseCase: GuardarTokenPushUseCase,
    private val crashReporter: CrashReporter,
    private val preferenciasLocales: PreferenciasLocales
) : ViewModel() {
    private var notificationsJob: Job? = null
    private var productosJob: Job? = null
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
    private fun notificationsFlow() = getNotificationsUseCase()
        .catch { emit(emptyList()) }

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
            loadUserDataSuspending()
        }
    }

                        // Actualiza el listaId para activar el flow compartido
    private suspend fun loadUserDataSuspending(ofrecerNotificaciones: Boolean = true) {
        try {
            getUserUseCase().fold(
                onSuccess = { usuario ->
                    setState { setUser(usuario.toUI()) }
                    val anonymous = isAnonymousUserUseCase()
                    setState { setAnonymous(anonymous) }
                    // El menú lateral muestra el estado de vinculación de Alexa. No hace
                    // falta pedirlo aparte: `usuario` ya lo trae, porque getActualUser lee
                    // el mismo documento de `usuarios/{uid}` donde vive el campo. Los
                    // anónimos no pueden vincular, así que se fuerza a false.
                    setState { setAlexaVinculada(!anonymous && usuario.alexaVinculada) }
                    analyticsService.setUserId(usuario.uid)

                    getListasUseCase().fold(
                        onSuccess = { listas ->
                            if (listas.isNotEmpty()) {
                                setState { setUser(state.value.user.copy(listas = listas.map { it.id })) }
                            }
                            val nombre = listas.firstOrNull()?.nombre ?: "Lista de la compra"
                            setState { setListaNombre(nombre) }
                            _currentListaId.value = listas.firstOrNull()?.id ?: usuario.listas.firstOrNull()
                            if (_currentListaId.value == null) setState { setLoaded() }
                        },
                        onFailure = {
                            _currentListaId.value = usuario.listas.firstOrNull()
                            if (_currentListaId.value == null) setState { setLoaded() }
                        }
                    )

                    productosJob = viewModelScope.launch {
                        sharedProductosFlow.collect { listaCompraUI ->
                            setState { getListaCompraUI(listaCompraUI) }
                        }
                    }

                    notificationsJob = viewModelScope.launch {
                        notificationsFlow().collect { notifications ->
                            setState { updateNotifications(notifications) }
                        }
                    }

                    maybeOfferNotificationsPermission(anonymous, ofrecerNotificaciones)

                    sharedState.showLoading(false)
                },
                onFailure = { error ->
                    val errorMessage = (error as? Exception)?.message ?: "Error desconocido"
                    val errorTitle = getString(Res.string.home_alert_error_get_user_title)
                    setState {
                        showErrorAlert(
                            errorTitle,
                            message = errorMessage
                        ).setLoaded()
                    }
                    sharedState.showLoading(false)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            setState { setLoaded() }
            sharedState.showLoading(false)
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
            is ListaCompraEvent.OnMenuClick -> {
                setState { showMenu() }
                refreshNotificationsStatus()
            }
            is ListaCompraEvent.OnLogoutClick -> logOut()
            is ListaCompraEvent.OnShareListClick -> onShareListClick()
            is ListaCompraEvent.DismissCustomDialog -> setState { showCustomDialog(false) }
            is ListaCompraEvent.ShareList -> shareList(event.email)
            is ListaCompraEvent.OnShareTextFieldChange -> setState { updateShareTextField(event.text) }
            is ListaCompraEvent.OnDeleteAccountClick -> setState { showDeleteAccountDialog(true) }
            is ListaCompraEvent.DismissDeleteAccountDialog -> setState { showDeleteAccountDialog(false) }
            is ListaCompraEvent.OnDeleteAccountConfirm -> deleteAccount()
            is ListaCompraEvent.TogglePurchased -> togglePurchased(event.productId)
            is ListaCompraEvent.OnMisListasClick -> navigator.navigateTo(Routes.MisListas)
            is ListaCompraEvent.OnAlexaClick -> navigator.navigateTo(Routes.Alexa)
            is ListaCompraEvent.OnNotificacionesClick -> onNotificacionesClick()
            is ListaCompraEvent.OnNotificationsPermissionResult ->
                onNotificationsPermissionResult(event.granted)
            is ListaCompraEvent.OnForceCrashClick -> crashReporter.forceCrash()
            is ListaCompraEvent.OnForceNonFatalClick -> crashReporter.recordException(
                Exception("Non-fatal de prueba desde el menú lateral"),
                "ListaCompraViewModel.OnForceNonFatalClick"
            )

            is ListaCompraEvent.OnLoginFromMenuClick -> setState { showDataLossWarningDialog(true) }
            is ListaCompraEvent.OnCancelLoginDataLoss -> setState { showDataLossWarningDialog(false) }
            is ListaCompraEvent.OnConfirmLoginDataLoss -> {
                setState { showDataLossWarningDialog(false) }
                navigator.navigateTo(Routes.Login)
            }
            is ListaCompraEvent.OnLoginFromEmptyListClick -> navigator.navigateTo(Routes.Login)

            is ListaCompraEvent.OnShareAccountRequiredConfirm -> {
                setState { showShareRequiresAccountDialog(false) }
                setState { setLinkAccountOrigin(LinkAccountOrigin.SHARE) }
                setState { showLinkAccountDialog(true) }
            }

            is ListaCompraEvent.OnShareAccountRequiredCancel ->
                setState { showShareRequiresAccountDialog(false) }

            is ListaCompraEvent.OnLinkEmailChange -> setState { updateLinkEmail(event.text) }
            is ListaCompraEvent.OnLinkPasswordChange -> setState { updateLinkPassword(event.text) }
            is ListaCompraEvent.OnLinkAccountConfirm -> linkAccountWithEmail()
            is ListaCompraEvent.OnOpenLinkAccountDialog -> {
                setState { setLinkAccountOrigin(LinkAccountOrigin.ALEXA) }
                setState { showLinkAccountDialog(true) }
            }

            is ListaCompraEvent.OnDismissLinkAccountDialog -> {
                setState { showLinkAccountDialog(false) }
                setState { clearLinkFields() }
            }

            is ListaCompraEvent.OnConfirmLinkCredentialInUse -> signInWithExistingAccountAndDiscardAnonymous()
            is ListaCompraEvent.OnCancelLinkCredentialInUse ->
                setState { showLinkCredentialInUseDialog(false) }

            is ListaCompraEvent.OnEditNombreClick -> setState { showEditNombreDialog(true) }
            is ListaCompraEvent.DismissEditNombreDialog -> setState { showEditNombreDialog(false) }
            is ListaCompraEvent.ConfirmEditNombre -> editNombre(event.nombre)

            is ListaCompraEvent.OnNotificationsOfferAccept -> onNotificationsOfferAccept()
            is ListaCompraEvent.OnNotificationsOfferDecline -> onNotificationsOfferDecline()
            is ListaCompraEvent.OnNotificationsPermissionRequestedFromMenu ->
                marcarNotificacionesOfrecidas()
            is ListaCompraEvent.OnNotificationsStatusClick ->
                setState { showNotificationsStatusDialog(true) }
            is ListaCompraEvent.OnCloseNotificationsStatusDialog ->
                setState { showNotificationsStatusDialog(false) }
            is ListaCompraEvent.OnOpenNotificationsSettingsClick ->
                setState { showNotificationsStatusDialog(false) }
        }
    }

    private fun editNombre(nombre: String) {
        val nombreValido = nombre.trim().take(40)
        if (nombreValido.isBlank()) return
        viewModelScope.launch {
            updateNombreUseCase(nombreValido).fold(
                onSuccess = {
                    setState { setUser(state.value.user.copy(nombre = nombreValido)) }
                    setState { showEditNombreDialog(false) }
                },
                onFailure = { error ->
                    val errorMessage = (error as? Exception)?.message ?: "Error desconocido"
                    val errorTitle = getString(Res.string.home_alert_error_update_title)
                    setState { showEditNombreDialog(false) }
                    setState {
                        showErrorAlert(
                            errorTitle,
                            message = errorMessage
                        )
                    }
                }
            )
        }
    }

    private fun onNotificationsPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            setState { setNotificacionesActivadas(granted) }
            if (granted) {
                val token = PushNotifications.getToken()
                if (token != null) {
                    guardarTokenPushUseCase(token)
                }
            } else {
                // Sustituye a la alerta de error de antes: mismo mensaje, pero con un botón
                // que lleva a los ajustes en vez de dejar al usuario sin saber a dónde ir.
                setState { showNotificationsStatusDialog(true) }
            }
        }
    }

    // Único sitio donde se refresca notificacionesActivadas aparte de OnNotificationsPermissionResult:
    // el permiso se puede cambiar desde Ajustes, fuera de la app, y ese cambio no dispara
    // ningún evento propio. Se comprueba al abrir el menú porque es el único sitio donde se ve.
    private fun refreshNotificationsStatus() {
        viewModelScope.launch {
            val estabanActivadas = state.value.notificacionesActivadas
            val activadas = PushNotifications.hasPermission()
            setState { setNotificacionesActivadas(activadas) }
            // Pasaron de desactivadas a activadas fuera de la app: sin guardar el token
            // aquí, no llegaría a fcmTokens hasta el siguiente arranque.
            if (!estabanActivadas && activadas) {
                val token = PushNotifications.getToken()
                if (token != null) {
                    guardarTokenPushUseCase(token)
                }
            }
        }
    }

    // La sesión anónima ya se ha cargado en este punto de loadUserDataSuspending; se pasa
    // en vez de leerla de state.value porque el setState que la fija puede no haberse
    // aplicado aún cuando esta función se invoca. ofrecerNotificaciones=false es para el
    // arranque que sigue a vincular cuenta desde "compartir": ese usuario está en mitad
    // de compartir, no recibiendo, y ya le sale otro diálogo justo después.
    private suspend fun maybeOfferNotificationsPermission(anonymous: Boolean, ofrecerNotificaciones: Boolean) {
        if (!ofrecerNotificaciones) return
        if (anonymous) return
        if (PushNotifications.hasPermission()) return
        if (preferenciasLocales.getBoolean(NOTIFICATIONS_OFFER_KEY, false)) return
        // No apilar sobre otro diálogo de Home: si hay alguno abierto, se queda sin marcar
        // como ofrecido y se reintenta en el siguiente arranque.
        if (state.value.hasAnyDialogAbierto()) return
        setState { showNotificationsOfferDialog(true) }
    }

    // El requester del diálogo del sistema es @Composable y vive en HomeScreen, junto con
    // el diálogo propio; el "Sí, avisadme" de HomeScreen llama a este evento y directamente
    // al requester en el mismo lambda, así que aquí solo queda marcar y cerrar.
    private fun onNotificationsOfferAccept() {
        setState { showNotificationsOfferDialog(false) }
        marcarNotificacionesOfrecidas()
    }

    private fun onNotificationsOfferDecline() {
        setState { showNotificationsOfferDialog(false) }
        marcarNotificacionesOfrecidas()
    }

    private fun marcarNotificacionesOfrecidas() {
        preferenciasLocales.setBoolean(NOTIFICATIONS_OFFER_KEY, true)
    }

    private fun onShareListClick() {
        if (state.value.isAnonymous) {
            setState { showShareRequiresAccountDialog(true) }
        } else {
            setState { showCustomDialog(true) }
        }
    }

    private fun linkAccountWithEmail() {
        val email = state.value.linkEmail.text.trim()
        val password = state.value.linkPassword.text
        viewModelScope.launch {
            linkAccountWithEmailUseCase(email, password).fold(
                onSuccess = { usuario ->
                    setState { setUser(usuario.toUI()) }
                    setState { setAnonymous(false) }
                    setState { showLinkAccountDialog(false) }
                    setState { clearLinkFields() }
                    stopNotificationsListener()
                    // Solo abre el diálogo de compartir si se vino de ahí. Desde la
                    // pantalla de Alexa el usuario no ha pedido compartir nada, y le
                    // salía de la nada nada más crear la cuenta.
                    val vieneDeCompartir = state.value.linkAccountOrigin == LinkAccountOrigin.SHARE
                    // Si viene de compartir, no se ofrece el permiso en esta carga: se
                    // apilaría con el diálogo de compartir que se abre justo debajo. Al no
                    // marcarse como ofrecido, sale en el siguiente arranque.
                    loadUserDataSuspending(ofrecerNotificaciones = !vieneDeCompartir)
                    if (vieneDeCompartir) {
                        setState { showCustomDialog(true) }
                    }
                },
                onFailure = { error ->
                    if (error is LoginFailure.CredentialAlreadyInUse) {
                        setState { showLinkCredentialInUseDialog(true) }
                    } else {
                        val errorMessage = (error as? Exception)?.message ?: "Error desconocido"
                        val errorTitle = getString(Res.string.home_alert_error_link_account_title)
                        setState {
                            showErrorAlert(
                                errorTitle,
                                errorMessage
                            )
                        }
                    }
                }
            )
        }
    }

    private fun signInWithExistingAccountAndDiscardAnonymous() {
        val email = state.value.linkEmail.text.trim()
        val password = state.value.linkPassword.text
        viewModelScope.launch {
            setState { showLinkCredentialInUseDialog(false) }
            stopNotificationsListener()
            userLoginUseCase(email, password).fold(
                onSuccess = {
                    setState { showLinkAccountDialog(false) }
                    setState { clearLinkFields() }
                    val vieneDeCompartir = state.value.linkAccountOrigin == LinkAccountOrigin.SHARE
                    loadUserDataSuspending(ofrecerNotificaciones = !vieneDeCompartir)
                    // Solo abre el diálogo de compartir si se vino de ahí. Desde la
                    // pantalla de Alexa el usuario no ha pedido compartir nada, y le
                    // salía de la nada nada más crear la cuenta.
                    if (vieneDeCompartir) {
                        setState { showCustomDialog(true) }
                    }
                },
                onFailure = { error ->
                    val errorMessage = (error as? Exception)?.message ?: "Error desconocido"
                    val errorTitle = getString(Res.string.home_alert_error_login_title)
                    setState {
                        showErrorAlert(
                            errorTitle,
                            errorMessage
                        )
                    }
                }
            )
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            // Parar listeners ANTES de borrar la cuenta
            stopNotificationsListener()
            deleteAccountUseCase().fold(
                onSuccess = {
                    setState { showDeleteAccountDialog(false) }
                    sharedState.showLoading(false)
                    setState { ListaCompraState() }
                    navigator.clearAndNavigateTo(Routes.Login)
                },
                onFailure = { error ->
                    val errorMessage = (error as? Exception)?.message ?: "Error desconocido"
                    val errorTitle = getString(Res.string.home_alert_error_delete_account_title)
                    sharedState.showLoading(false)
                    setState { showDeleteAccountDialog(false) }
                    setState {
                        showErrorAlert(
                            errorTitle,
                            message = errorMessage
                        )
                    }
                }
            )
        }
    }

    private fun onNotificacionesClick() {
        if (!isAnonymousUserUseCase()) {
            navigator.navigateTo(Routes.Notificaciones)
        }
    }

    private fun shareList(rawEmail: String) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val ultimaInvitacion = preferenciasLocales.getLong(ULTIMA_INVITACION_KEY, 0L)
        if (currentTime - ultimaInvitacion < INTERVALO_ENTRE_INVITACIONES.inWholeMilliseconds) {
            viewModelScope.launch {
                setEffect(ListaCompraEffect.ShowError(getString(Res.string.home_alert_share_rate_limit_message)))
            }
            return
        }
        val email = rawEmail.trim()
        val user = state.value.user
        viewModelScope.launch {
            shareListaCompraUseCase(user.nombre, user.listaId, email).fold(
                onSuccess = {
                    preferenciasLocales.setLong(
                        ULTIMA_INVITACION_KEY,
                        Clock.System.now().toEpochMilliseconds()
                    )
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
                    val errorTitle = getString(Res.string.home_alert_error_share_title)
                    setState {
                        showErrorAlert(
                            errorTitle,
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
                crashReporter.recordException(e, "ListaCompraViewModel.updateProducto")
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
                    crashReporter.recordException(e, "ListaCompraViewModel.saveEditedProduct")
                    if (originalProduct != null) {
                        setState {
                            startEditingProduct(editingId, originalProduct.nombre)
                        }
                    } else {
                        setState { cancelEditing() }
                    }

                    val errorTitle = getString(Res.string.home_alert_error_update_title)
                    setState {
                        showErrorAlert(
                            errorTitle,
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
                    val errorTitle = getString(Res.string.home_alert_error_add_product_title)
                    setState {
                        showErrorAlert(
                            errorTitle,
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
                crashReporter.recordException(e, "ListaCompraViewModel.borrarProducto")
                val errorTitle = getString(Res.string.home_alert_error_delete_product_title)
                setState {
                    showErrorAlert(
                        errorTitle,
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
                crashReporter.recordException(e, "ListaCompraViewModel.togglePurchased")
                setState { togglePurchased(productId) }
                val errorTitle = getString(Res.string.home_alert_error_update_title)
                setState {
                    showErrorAlert(
                        errorTitle,
                        "No se pudo guardar el estado del producto. Verifica tu conexión e inténtalo de nuevo."
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
                val errorTitle = getString(Res.string.home_alert_error_delete_list_title)
                setState {
                    showErrorAlert(
                        errorTitle,
                        "No se pudo eliminar la lista completa. Verifica tu conexión a internet e inténtalo nuevamente."
                    )
                }
            }
        }
    }
}
