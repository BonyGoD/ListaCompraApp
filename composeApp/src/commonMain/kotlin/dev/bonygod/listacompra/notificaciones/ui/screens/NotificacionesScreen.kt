package dev.bonygod.listacompra.notificaciones.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import dev.bonygod.listacompra.notificaciones.ui.NotificacionesViewModel
import dev.bonygod.listacompra.notificaciones.ui.composables.NotificacionesContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotificacionesScreen() {
    val viewModel: NotificacionesViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.startListening()
    }
    NotificacionesContent(
        state = state.value,
        onEvent = viewModel::onEvent
    )
}
