package dev.bonygod.listacompra.home.ui.composables.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.getPlatform
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraState
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.logout
import listacompra.composeapp.generated.resources.share_list
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MenuLateral(
    state: ListaCompraState,
    setEvent: (ListaCompraEvent) -> Unit
) {
    val platform = getPlatform()
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp, top = 50.dp),
            fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
            fontWeight = FontWeight.Bold,
            text = state.user.nombre
        )
        Text(
            modifier = Modifier,
            color = Color.LightGray,
            fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
            fontSize = 10.sp,
            text = state.user.email
        )
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 50.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.share_list),
                tint = Color.Gray,
                contentDescription = "Icono menú",
            )
            Text(
                modifier = Modifier.padding(start = 5.dp),
                fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                text = "Compartir lista"
            )
        }
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 30.dp)
                .clickable {
                    setEvent(ListaCompraEvent.OnLogoutClick)
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.logout),
                tint = Color.Gray,
                contentDescription = "Icono menú",
            )
            Text(
                modifier = Modifier.padding(start = 5.dp),
                fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                text = "Cerrar sesión"
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            modifier = Modifier.align(Alignment.End),
            text = "Version v${getPlatform().appVersion}",
            color = Color.Black,
            fontSize = 10.sp,
            fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
        )
    }
}