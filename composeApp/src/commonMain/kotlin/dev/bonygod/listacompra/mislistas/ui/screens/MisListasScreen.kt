package dev.bonygod.listacompra.mislistas.ui.screens
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import dev.bonygod.listacompra.mislistas.ui.MisListasViewModel
import dev.bonygod.listacompra.mislistas.ui.composables.MisListasContent
import org.koin.compose.viewmodel.koinViewModel
@Composable
fun MisListasScreen() {
    val viewModel: MisListasViewModel = koinViewModel()
    val state = viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadListas()
    }
    MisListasContent(
        state = state.value,
        onEvent = viewModel::onEvent
    )
}
