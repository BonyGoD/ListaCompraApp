package dev.bonygod.listacompra.home.ui.composables.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.common.ui.theme.PrimaryBlue
import dev.bonygod.listacompra.core.AppConstants
import dev.bonygod.listacompra.getPlatform
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.notificaciones.rememberNotificationPermissionRequester
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.basura_black
import listacompra.composeapp.generated.resources.edit_icon
import listacompra.composeapp.generated.resources.listas
import listacompra.composeapp.generated.resources.logout
import listacompra.composeapp.generated.resources.menu_lateral_alexa_linked
import listacompra.composeapp.generated.resources.menu_lateral_alexa_not_linked
import listacompra.composeapp.generated.resources.menu_lateral_delete_account
import listacompra.composeapp.generated.resources.menu_lateral_edit_nombre_dialog_cancel_button
import listacompra.composeapp.generated.resources.menu_lateral_edit_nombre_dialog_confirm_button
import listacompra.composeapp.generated.resources.menu_lateral_edit_nombre_dialog_title
import listacompra.composeapp.generated.resources.menu_lateral_edit_nombre_description
import listacompra.composeapp.generated.resources.menu_lateral_edit_nombre_field_label
import listacompra.composeapp.generated.resources.menu_lateral_login_or_register
import listacompra.composeapp.generated.resources.menu_lateral_logout
import listacompra.composeapp.generated.resources.menu_lateral_my_lists
import listacompra.composeapp.generated.resources.menu_lateral_notifications
import listacompra.composeapp.generated.resources.menu_lateral_notifications_disabled
import listacompra.composeapp.generated.resources.menu_lateral_notifications_enabled
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
    val requestNotificationPermission = rememberNotificationPermissionRequester { granted ->
        setEvent(ListaCompraEvent.OnNotificationsPermissionResult(granted))
    }
    Column(modifier = Modifier.padding(16.dp)) {
        Icon(
            painter = notificationIcon,
            tint = if (state.notifications.isEmpty()) Color.Black else Color.Red,
            contentDescription = "Icono menú",
            modifier = Modifier.align(Alignment.End)
                .clickable {
                    setEvent(ListaCompraEvent.OnNotificacionesClick)
                    onCloseDrawer()
                }
        )
        Row(
            modifier = Modifier
                .padding(top = 38.dp, bottom = 2.dp)
                .clickable { setEvent(ListaCompraEvent.OnEditNombreClick) }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                fontWeight = FontWeight.Bold,
                text = state.user.nombre
            )
            Icon(
                painter = painterResource(Res.drawable.edit_icon),
                tint = Color.Gray,
                contentDescription = stringResource(Res.string.menu_lateral_edit_nombre_description),
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(16.dp)
            )
        }
        if (state.showEditNombreDialog) {
            EditNombreDialog(
                initialNombre = state.user.nombre,
                onConfirm = { nombre -> setEvent(ListaCompraEvent.ConfirmEditNombre(nombre)) },
                onDismiss = { setEvent(ListaCompraEvent.DismissEditNombreDialog) }
            )
        }
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
            fontSize = 14.sp,
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
        if (!state.isAnonymous) {
            val notificationsStatusLabel = if (state.notificacionesActivadas) {
                stringResource(Res.string.menu_lateral_notifications_enabled)
            } else {
                stringResource(Res.string.menu_lateral_notifications_disabled)
            }
            Row(
                modifier = Modifier.padding(start = 10.dp, top = 30.dp)
                    .clickable {
                        if (state.notificacionesActivadas) {
                            // Ya están activadas: pedir el permiso otra vez no serviría de
                            // nada (el sistema contestaría "sí" sin enseñar nada). Solo se
                            // enseña el estado.
                            setEvent(ListaCompraEvent.OnNotificationsStatusClick)
                        } else {
                            requestNotificationPermission()
                        }
                        // Ya se pidió desde aquí: si no se marca, el diálogo propio de Home
                        // volvería a ofrecerlo en el siguiente arranque sin sentido.
                        setEvent(ListaCompraEvent.OnNotificationsPermissionRequestedFromMenu)
                        onCloseDrawer()
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.notification_blank),
                    tint = Color.Gray,
                    contentDescription = stringResource(Res.string.menu_lateral_notifications),
                )
                Text(
                    modifier = Modifier.padding(start = 5.dp),
                    fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    text = notificationsStatusLabel
                )
            }
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

@Composable
private fun EditNombreDialog(
    initialNombre: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf(initialNombre) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.menu_lateral_edit_nombre_dialog_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { if (it.length <= 40) nombre = it },
                label = { Text(stringResource(Res.string.menu_lateral_edit_nombre_field_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (nombre.isNotBlank()) onConfirm(nombre.trim())
                })
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (nombre.isNotBlank()) onConfirm(nombre.trim()) },
                enabled = nombre.isNotBlank()
            ) {
                Text(stringResource(Res.string.menu_lateral_edit_nombre_dialog_confirm_button), color = PrimaryBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.menu_lateral_edit_nombre_dialog_cancel_button), color = Color.Gray)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MenuLateralPreview() {
    MenuLateral(state = ListaCompraState(), setEvent = {})
}
