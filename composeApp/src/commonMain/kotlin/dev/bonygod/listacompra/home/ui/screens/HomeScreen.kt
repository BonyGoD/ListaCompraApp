package dev.bonygod.listacompra.home.ui.screens

import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import dev.bonygod.listacompra.home.ui.ListaCompraViewModel
import dev.bonygod.listacompra.home.ui.composables.HomeContent
import dev.bonygod.listacompra.home.ui.composables.components.MenuLateral
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(snackbarHostState: SnackbarHostState) {
    val viewModel: ListaCompraViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

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