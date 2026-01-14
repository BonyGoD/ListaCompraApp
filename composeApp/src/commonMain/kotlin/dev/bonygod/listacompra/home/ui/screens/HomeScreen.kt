package dev.bonygod.listacompra.home.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import dev.bonygod.listacompra.home.ui.ListaCompraViewModel
import dev.bonygod.listacompra.home.ui.composables.HomeContent
import dev.bonygod.listacompra.login.ui.composables.RegisterContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen() {
    val viewModel: ListaCompraViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()

//    HomeContent(
//        data = state.value.listaCompraUI,
//        state = state.value,
//        onEvent = viewModel::onEvent
//    )
    RegisterContent()
}