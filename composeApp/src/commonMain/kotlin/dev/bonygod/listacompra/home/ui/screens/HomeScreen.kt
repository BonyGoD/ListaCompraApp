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
import dev.bonygod.listacompra.home.ui.ListaCompraViewModel
import dev.bonygod.listacompra.home.ui.composables.HomeContent
import dev.bonygod.listacompra.home.ui.composables.components.MenuLateral
import dev.bonygod.listacompra.home.ui.composables.components.ShareListaCompraDialog
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEffect
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(snackbarHostState: SnackbarHostState) {
    val viewModel: ListaCompraViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

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

    if(state.value.customDialog){
        ShareListaCompraDialog(
            state = state.value,
            setEvent = viewModel::onEvent
        )
    }

    ModalNavigationDrawer(
        drawerState = state.value.drawerState,
        drawerContent = {
            ModalDrawerSheet {
                MenuLateral(
                    state = state.value,
                    setEvent = viewModel::onEvent
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
                    when(it) {
                        ListaCompraEvent.OnMenuClick -> scope.launch { state.value.drawerState.open() }
                        else -> viewModel.onEvent(it)
                    }
                }
            )
        }
    }
}