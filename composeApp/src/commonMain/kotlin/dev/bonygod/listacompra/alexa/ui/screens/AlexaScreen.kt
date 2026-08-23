package dev.bonygod.listacompra.alexa.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import dev.bonygod.listacompra.alexa.ui.AlexaViewModel
import dev.bonygod.listacompra.alexa.ui.composables.AlexaContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AlexaScreen() {
    val viewModel: AlexaViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.load()
    }
    AlexaContent(
        state = state.value,
        onEvent = viewModel::onEvent
    )
}
