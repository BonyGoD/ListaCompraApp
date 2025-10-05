package dev.bonygod.listacompra.ui.composables.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.ui.model.ProductoUI
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.basura
import listacompra.composeapp.generated.resources.basura_black
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
            .height(60.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = producto.nombre.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            modifier = Modifier.weight(1f),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painter = painterResource(Res.drawable.edit_icon),
            contentDescription = "Icono editar",
            modifier = Modifier
                .size(20.dp)
                .clickable {
                    onEvent(ListaCompraEvent.StartEditingProduct(producto.id, producto.nombre))
                }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painter = painterResource(Res.drawable.basura_black),
            contentDescription = "Icono borrar",
            modifier = Modifier
                .size(20.dp)
                .clickable { onEvent(ListaCompraEvent.BorrarProducto(producto.id)) }
        )
    }
}
