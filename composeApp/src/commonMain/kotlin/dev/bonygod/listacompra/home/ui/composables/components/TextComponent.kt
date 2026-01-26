package dev.bonygod.listacompra.home.ui.composables.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.common.ui.theme.PrimaryBlue
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.home.ui.model.ProductoUI
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.basura_black
import listacompra.composeapp.generated.resources.edit_icon
import org.jetbrains.compose.resources.painterResource

@Composable
fun TextComponent(
    producto: ProductoUI,
    onEvent: (ListaCompraEvent) -> Unit
) {
    val backgroundColor = when {
        producto.isImportant -> Color(0xFFFFB3BA)
        else -> Color.White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .border(1.dp, PrimaryBlue, RoundedCornerShape(18.dp))
            .background(backgroundColor, RoundedCornerShape(18.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onEvent(
                            ListaCompraEvent.UpdateProducto(
                                productoId = producto.id,
                                nombre = producto.nombre,
                                isImportant = !producto.isImportant
                            )
                        )
                    }
                )
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = producto.nombre.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            modifier = Modifier.weight(1f),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(Res.drawable.edit_icon),
            tint = PrimaryBlue,
            contentDescription = "Icono editar",
            modifier = Modifier
                .size(20.dp)
                .clickable {
                    onEvent(ListaCompraEvent.StartEditingProduct(producto.id, producto.nombre))
                },
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            painter = painterResource(Res.drawable.basura_black),
            tint = PrimaryBlue,
            contentDescription = "Icono borrar",
            modifier = Modifier
                .size(20.dp)
                .clickable { onEvent(ListaCompraEvent.BorrarProducto(producto.id)) }
        )
    }
}
