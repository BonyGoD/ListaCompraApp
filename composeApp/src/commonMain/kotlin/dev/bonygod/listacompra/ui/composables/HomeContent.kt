package dev.bonygod.listacompra.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.ScreenWrapper
import dev.bonygod.listacompra.ui.composables.components.AddProductBottomSheet
import dev.bonygod.listacompra.ui.composables.components.ConfirmDialog
import dev.bonygod.listacompra.ui.composables.components.ErrorAlert
import dev.bonygod.listacompra.ui.composables.components.SuccessAlert
import dev.bonygod.listacompra.ui.composables.components.TextComponent
import dev.bonygod.listacompra.ui.composables.components.TextFieldComponent
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.ui.composables.preview.ListaCompraPreview
import dev.bonygod.listacompra.ui.model.ListaCompraUI
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.add_button
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HomeContent(
    data: ListaCompraUI,
    state: ListaCompraState,
    onEvent: (ListaCompraEvent) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(end = 10.dp, top = 10.dp, bottom = 20.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "Lista de la compra",
                    fontSize = 25.sp,
                    fontWeight = Bold
                )
                Image(
                    painter = painterResource(Res.drawable.add_button),
                    contentDescription = "Icono agregar producto",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            onEvent(ListaCompraEvent.ShowBottomSheet(true))
                        }
                        .align(Alignment.CenterEnd)
                        .padding(10.dp)
                )
            }
            LazyColumn(modifier = Modifier.padding(10.dp)) {
                items(data.productos) { producto ->
                    if (state.editingProductId == producto.id) {
                        TextFieldComponent(
                            onEvent = onEvent,
                            state = state
                        )
                    } else {
                        TextComponent(
                            producto = producto,
                            onEvent = onEvent
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 5.dp),
                        thickness = 1.dp,
                        color = Color.Black
                    )
                }
            }
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            content = {
                Text(
                    fontWeight = Bold,
                    fontSize = 15.sp,
                    text = "Borrar lista"
                )
            },
            onClick = {
                onEvent(ListaCompraEvent.ShowDialog(true))
            }
        )
    }

    if (state.dialogState) {
        ConfirmDialog(
            title = "¿Estás seguro de borrar todos los productos?",
            message = "Esta acción no se puede deshacer.",
            onConfirm = { onEvent(ListaCompraEvent.ConfirmDelete) },
            onCancel = { onEvent(ListaCompraEvent.CancelDialog) }
        )
    }

    if (state.showErrorAlert) {
        ErrorAlert(
            title = state.errorAlertTitle,
            message = state.errorAlertMessage,
            onDismiss = { onEvent(ListaCompraEvent.HideErrorAlert) }
        )
    }

    if (state.showSuccessAlert) {
        SuccessAlert(
            title = state.successAlertTitle,
            message = state.successAlertMessage,
            onDismiss = { onEvent(ListaCompraEvent.HideSuccessAlert) }
        )
    }

    if (state.showBottomSheet) {
        AddProductBottomSheet(
            state = state,
            onEvent = onEvent,
            onDismiss = { onEvent(ListaCompraEvent.ShowBottomSheet(false)) }
        )
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    ScreenWrapper {
        HomeContent(
            data = ListaCompraPreview.ListaCompraUI,
            state = ListaCompraState(listaCompraUI = ListaCompraPreview.ListaCompraUI),
        )
    }
}