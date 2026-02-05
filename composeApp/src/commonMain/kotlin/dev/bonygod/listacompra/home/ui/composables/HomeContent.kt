package dev.bonygod.listacompra.home.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.ads.AdConstants
import dev.bonygod.listacompra.ads.getBannerAdUnitId
import dev.bonygod.listacompra.ads.ui.BannerAd
import dev.bonygod.listacompra.common.ui.theme.PrimaryBlue
import dev.bonygod.listacompra.common.ui.theme.SecondaryBlue
import dev.bonygod.listacompra.home.ui.composables.components.AddProductBottomSheet
import dev.bonygod.listacompra.home.ui.composables.components.ConfirmDialog
import dev.bonygod.listacompra.home.ui.composables.components.ErrorAlert
import dev.bonygod.listacompra.home.ui.composables.components.ShowNotificationsBottomSheet
import dev.bonygod.listacompra.home.ui.composables.components.SuccessAlert
import dev.bonygod.listacompra.home.ui.composables.components.TextComponent
import dev.bonygod.listacompra.home.ui.composables.components.TextFieldComponent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.home.ui.model.ListaCompraUI
import dev.bonygod.listacompra.home.ui.model.ProductoUI
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.menu_hamburguesa
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    data: ListaCompraUI,
    state: ListaCompraState,
    onEvent: (ListaCompraEvent) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().background(SecondaryBlue)) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = SecondaryBlue),
            title = { Text(text = "Lista de la compra") },
            navigationIcon = {
                IconButton(
                    onClick = {
                        onEvent(ListaCompraEvent.OnMenuClick)
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.menu_hamburguesa),
                        contentDescription = "Abrir menú",
                        tint = PrimaryBlue
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
                    Text(
                        text = "Borrar lista",
                        fontSize = 15.sp,
                        fontWeight = Bold,
                        color = PrimaryBlue
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
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
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

        BannerAd(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            adUnitId = AdConstants.getBannerAdUnitId()
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
    if (state.showNotifications) {
        ShowNotificationsBottomSheet(
            state = state,
            onEvent = onEvent,
            onDismiss = { onEvent(ListaCompraEvent.ShowNotificationsBottomSheet(false)) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    HomeContent(
        data = ListaCompraUI(
            productos = listOf(
                ProductoUI(
                    id = "1",
                    nombre = "Leche",
                    isImportant = false
                ),
                ProductoUI(
                    id = "2",
                    nombre = "Pan",
                    isImportant = false
                )
            )
        ),
        state = ListaCompraState(),
        onEvent = {}
    )
}
