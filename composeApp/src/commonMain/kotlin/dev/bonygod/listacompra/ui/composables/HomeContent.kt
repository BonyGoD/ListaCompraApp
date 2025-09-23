package dev.bonygod.listacompra.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import dev.bonygod.listacompra.ui.composables.components.ConfirmDialog
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.ui.composables.preview.ListaCompraPreview
import dev.bonygod.listacompra.ui.model.ListaCompraUI
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.basura
import listacompra.composeapp.generated.resources.update_icon
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
            Box(modifier = Modifier.fillMaxWidth().padding(end = 10.dp, top = 10.dp)) {
                Image(
                    painter = painterResource(Res.drawable.update_icon),
                    contentDescription = "Icono update",
                    modifier = Modifier.size(20.dp).align(Alignment.CenterEnd)
                )
            }
            LazyColumn(modifier = Modifier.padding(10.dp)) {
                items(data.productos) { producto ->
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Text(
                            modifier = Modifier.padding(10.dp),
                            text = producto.nombre.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                            fontSize = 24.sp
                        )
                        Image(
                            painter = painterResource(Res.drawable.basura),
                            contentDescription = "Icono borrar",
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.CenterEnd)
                                .clickable(onClick = { onEvent(ListaCompraEvent.BorrarProducto(producto.id)) })
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
            title = "¿Estas seguro de borrar todos los productos?",
            message = "Esta acción no se puede deshacer.",
            onConfirm = { onEvent(ListaCompraEvent.ConfirmDelete) },
            onCancel = { onEvent(ListaCompraEvent.CancelDialog) }
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