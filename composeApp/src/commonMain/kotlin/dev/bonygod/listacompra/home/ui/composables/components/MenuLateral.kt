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
import dev.bonygod.listacompra.core.AppConstants
import dev.bonygod.listacompra.getPlatform
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraState
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.basura_black
import listacompra.composeapp.generated.resources.listas
import listacompra.composeapp.generated.resources.logout
import listacompra.composeapp.generated.resources.menu_lateral_alexa_linked
import listacompra.composeapp.generated.resources.menu_lateral_alexa_not_linked
import listacompra.composeapp.generated.resources.menu_lateral_delete_account
import listacompra.composeapp.generated.resources.menu_lateral_login_or_register
import listacompra.composeapp.generated.resources.menu_lateral_logout
import listacompra.composeapp.generated.resources.menu_lateral_my_lists
import listacompra.composeapp.generated.resources.menu_lateral_share_list
import listacompra.composeapp.generated.resources.menu_lateral_version_label
import listacompra.composeapp.generated.resources.notification_blank
import listacompra.composeapp.generated.resources.notification_with_noti
import listacompra.composeapp.generated.resources.share_list
import listacompra.composeapp.generated.resources.microfono
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MenuLateral(
    state: ListaCompraState,
    setEvent: (ListaCompraEvent) -> Unit,
    onCloseDrawer: () -> Unit = {}
) {
    val notificationIcon = if (state.notifications.isNotEmpty()) {
        painterResource(Res.drawable.notification_with_noti)
    } else {
        painterResource(Res.drawable.notification_blank)
    }
    Column(modifier = Modifier.padding(16.dp)) {
        Icon(
            painter = notificationIcon,
            tint = if (state.notifications.isEmpty()) Color.Black else Color.Red,
            contentDescription = "Icono menú",
            modifier = Modifier.align(Alignment.End)
                .clickable {
                    setEvent(ListaCompraEvent.ShowNotificationsBottomSheet(true))
                    onCloseDrawer()
                }
        )
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
            modifier = Modifier.padding(start = 10.dp, top = 30.dp)
                .clickable {
                    setEvent(ListaCompraEvent.OnMisListasClick)
                    onCloseDrawer()
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.listas),
                tint = Color.Gray,
                contentDescription = stringResource(Res.string.menu_lateral_my_lists),
            )
            Text(
                modifier = Modifier.padding(start = 5.dp),
                fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                text = stringResource(Res.string.menu_lateral_my_lists)
            )
        }
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 30.dp)
                .clickable {
                    setEvent(ListaCompraEvent.OnShareListClick)
                    onCloseDrawer()
                },
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
                text = stringResource(Res.string.menu_lateral_share_list)
            )
        }
        val alexaLabel = if (state.alexaVinculada) {
            stringResource(Res.string.menu_lateral_alexa_linked)
        } else {
            stringResource(Res.string.menu_lateral_alexa_not_linked)
        }
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 30.dp)
                .clickable {
                    setEvent(ListaCompraEvent.OnAlexaClick)
                    onCloseDrawer()
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.microfono),
                tint = Color.Gray,
                contentDescription = alexaLabel,
            )
            Text(
                modifier = Modifier.padding(start = 5.dp),
                fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                text = alexaLabel
            )
        }
        val logoutOrLoginEvent = if (state.isAnonymous) {
            ListaCompraEvent.OnLoginFromMenuClick
        } else {
            ListaCompraEvent.OnLogoutClick
        }
        val logoutOrLoginText = if (state.isAnonymous) {
            stringResource(Res.string.menu_lateral_login_or_register)
        } else {
            stringResource(Res.string.menu_lateral_logout)
        }
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 30.dp)
                .clickable {
                    setEvent(logoutOrLoginEvent)
                    onCloseDrawer()
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
                text = logoutOrLoginText
            )
        }
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 30.dp)
                .clickable {
                    setEvent(ListaCompraEvent.OnDeleteAccountClick)
                    onCloseDrawer()
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.basura_black),
                tint = Color.Gray,
                contentDescription = "Icono menú",
            )
            Text(
                modifier = Modifier.padding(start = 10.dp),
                fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                text = stringResource(Res.string.menu_lateral_delete_account)
            )
        }
        if (state.user.email == AppConstants.DEVELOPER_EMAIL) {
            Row(
                modifier = Modifier.padding(start = 10.dp, top = 30.dp)
                    .clickable {
                        setEvent(ListaCompraEvent.OnForceCrashClick)
                        onCloseDrawer()
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.padding(start = 10.dp),
                    fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    text = "Forzar crash (test)"
                )
            }
            Row(
                modifier = Modifier.padding(start = 10.dp, top = 10.dp)
                    .clickable {
                        setEvent(ListaCompraEvent.OnForceNonFatalClick)
                        onCloseDrawer()
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.padding(start = 10.dp),
                    fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    text = "Forzar non-fatal (test)"
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            modifier = Modifier.align(Alignment.End),
            text = "${stringResource(Res.string.menu_lateral_version_label)} v${getPlatform().appVersion}",
            color = Color.Black,
            fontSize = 10.sp,
            fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MenuLateralPreview() {
    MenuLateral(state = ListaCompraState(), setEvent = {})
}
