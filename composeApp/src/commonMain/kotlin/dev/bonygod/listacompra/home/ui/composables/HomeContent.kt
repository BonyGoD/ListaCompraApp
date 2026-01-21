package dev.bonygod.listacompra.home.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.home.ui.composables.components.AddProductBottomSheet
import dev.bonygod.listacompra.home.ui.composables.components.ConfirmDialog
import dev.bonygod.listacompra.home.ui.composables.components.ErrorAlert
import dev.bonygod.listacompra.home.ui.composables.components.SuccessAlert
import dev.bonygod.listacompra.home.ui.composables.components.TextComponent
import dev.bonygod.listacompra.home.ui.composables.components.TextFieldComponent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.home.ui.model.ListaCompraUI
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.basura
import listacompra.composeapp.generated.resources.menu_hamburguesa
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    data: ListaCompraUI,
    state: ListaCompraState,
    onEvent: (ListaCompraEvent) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = "Lista de la compra") },
            navigationIcon = {
                IconButton(
                    onClick = {
                        onEvent(ListaCompraEvent.OnMenuClick)
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.menu_hamburguesa),
                        contentDescription = "Abrir menú"
                    )
                }
            },
            actions = {
                Column(
                    modifier = Modifier
                        .clickable {
                            onEvent(ListaCompraEvent.ShowDialog(true))
                        }
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(Res.drawable.basura),
                        contentDescription = "Icono borrar todo",
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = "Borrar lista",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        )
        Column(modifier = Modifier.weight(1f)) {
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
                    Spacer(
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00913F)),
            content = {
                Text(
                    fontWeight = Bold,
                    fontSize = 15.sp,
                    text = "Agregar producto"
                )
            },
            onClick = {
                onEvent(ListaCompraEvent.ShowBottomSheet(true))
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