package dev.bonygod.listacompra.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import dev.bonygod.listacompra.ui.ListaCompraViewModel
import dev.bonygod.listacompra.ui.composables.HomeContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen() {
    val viewModel: ListaCompraViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()

    HomeContent(state.value.listaCompraUI)
}