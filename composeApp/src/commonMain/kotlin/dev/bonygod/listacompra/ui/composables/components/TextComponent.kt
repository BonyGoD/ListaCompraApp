package dev.bonygod.listacompra.ui.composables.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.ui.model.ProductoUI
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.basura
import listacompra.composeapp.generated.resources.edit_icon
import org.jetbrains.compose.resources.painterResource

@Composable
fun TextComponent(
    producto: ProductoUI,
    onEvent: (ListaCompraEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = producto.nombre.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            modifier = Modifier.weight(1f),
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painter = painterResource(Res.drawable.edit_icon),
            contentDescription = "Icono editar",
            modifier = Modifier
                .size(30.dp)
                .clickable {
                    onEvent(ListaCompraEvent.StartEditingProduct(producto.id, producto.nombre))
                }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painter = painterResource(Res.drawable.basura),
            contentDescription = "Icono borrar",
            modifier = Modifier
                .size(30.dp)
                .clickable { onEvent(ListaCompraEvent.BorrarProducto(producto.id)) }
        )
    }
}