package dev.bonygod.listacompra.home.ui.screens

import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import dev.bonygod.listacompra.common.ui.DataLossWarningDialog
import dev.bonygod.listacompra.core.navigation.PendingHomeAction
import dev.bonygod.listacompra.home.ui.ListaCompraViewModel
import dev.bonygod.listacompra.home.ui.composables.HomeContent
import dev.bonygod.listacompra.home.ui.composables.components.DeleteAccountDialog
import dev.bonygod.listacompra.home.ui.composables.components.LinkAccountDialog
import dev.bonygod.listacompra.home.ui.composables.components.MenuLateral
import dev.bonygod.listacompra.home.ui.composables.components.ShareListaCompraDialog
import dev.bonygod.listacompra.home.ui.composables.components.ShareRequiresAccountDialog
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEffect
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import kotlinx.coroutines.launch
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.data_loss_dialog_title
import listacompra.composeapp.generated.resources.link_account_credential_in_use_message
import listacompra.composeapp.generated.resources.link_account_credential_in_use_title
import listacompra.composeapp.generated.resources.menu_lateral_data_loss_message
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    snackbarHostState: SnackbarHostState,
    userId: String
) {
    val viewModel: ListaCompraViewModel = koinViewModel()
    val pendingHomeAction: PendingHomeAction = koinInject()
    val state = viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    // Llama a reloadUserData cada vez que cambia el userId
    LaunchedEffect(userId) {
        viewModel.loadUserData()
    }

    // Entrada desde la pantalla propia de Alexa: usuario anónimo que necesita crear una
    // cuenta antes de poder usar Alexa. Reutiliza el mismo diálogo y flujo de linkWithEmail
    // que ya existe para "compartir requiere cuenta", solo cambia cómo se abre.
    //
    // La petición viaja por PendingHomeAction y no dentro de la ruta: ahí se consume sola.
    // Léete el porqué en PendingHomeAction, que salió de dos bugs seguidos.
    LaunchedEffect(Unit) {
        if (pendingHomeAction.consumeLinkAccount()) {
            viewModel.onEvent(ListaCompraEvent.OnOpenLinkAccountDialog)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ListaCompraEffect.ShowError -> {
                    snackbarHostState.showSnackbar(message = effect.message)
                }

                is ListaCompraEffect.DismissDialog -> {
                    viewModel.onEvent(ListaCompraEvent.DismissCustomDialog)
                }

                is ListaCompraEffect.NavigateTo -> {
                    // Navigation is handled directly in the ViewModel via the navigator
                }
            }
        }
    }

    if (state.value.customDialog) {
        ShareListaCompraDialog(
            state = state.value,
            setEvent = viewModel::onEvent
        )
    }

    if (state.value.showDeleteAccount) {
        DeleteAccountDialog(
            state = state.value,
            setEvent = viewModel::onEvent
        )
    }

    if (state.value.showDataLossWarning) {
        DataLossWarningDialog(
            title = stringResource(Res.string.data_loss_dialog_title),
            message = stringResource(Res.string.menu_lateral_data_loss_message),
            onConfirm = { viewModel.onEvent(ListaCompraEvent.OnConfirmLoginDataLoss) },
            onCancel = { viewModel.onEvent(ListaCompraEvent.OnCancelLoginDataLoss) }
        )
    }

    if (state.value.showShareRequiresAccount) {
        ShareRequiresAccountDialog(setEvent = viewModel::onEvent)
    }

    if (state.value.showLinkAccount) {
        LinkAccountDialog(state = state.value, setEvent = viewModel::onEvent)
    }

    if (state.value.showLinkCredentialInUse) {
        DataLossWarningDialog(
            title = stringResource(Res.string.link_account_credential_in_use_title),
            message = stringResource(Res.string.link_account_credential_in_use_message),
            onConfirm = { viewModel.onEvent(ListaCompraEvent.OnConfirmLinkCredentialInUse) },
            onCancel = { viewModel.onEvent(ListaCompraEvent.OnCancelLinkCredentialInUse) }
        )
    }

    ModalNavigationDrawer(
        drawerState = state.value.drawerState,
        drawerContent = {
            ModalDrawerSheet {
                MenuLateral(
                    state = state.value,
                    setEvent = viewModel::onEvent,
                    onCloseDrawer = { scope.launch { state.value.drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) {
            HomeContent(
                data = state.value.listaCompraUI,
                state = state.value,
                onEvent = {
                    when (it) {
                        ListaCompraEvent.OnMenuClick -> scope.launch { state.value.drawerState.open() }
                        else -> viewModel.onEvent(it)
                    }
                }
            )
        }
    }
}
