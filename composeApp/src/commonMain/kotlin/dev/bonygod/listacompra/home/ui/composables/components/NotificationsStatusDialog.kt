package dev.bonygod.listacompra.home.ui.composables.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.bonygod.listacompra.common.ui.theme.PrimaryBlue
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.notification_blank
import listacompra.composeapp.generated.resources.notifications_permission_denied_message
import listacompra.composeapp.generated.resources.notifications_permission_denied_title
import listacompra.composeapp.generated.resources.notifications_status_dialog_close_button
import listacompra.composeapp.generated.resources.notifications_status_dialog_enabled_message
import listacompra.composeapp.generated.resources.notifications_status_dialog_enabled_title
import listacompra.composeapp.generated.resources.notifications_status_dialog_open_settings_button
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Estado del permiso de notificaciones, con acceso a los ajustes del sistema. Lo abren tanto
 * la entrada del menú lateral (activadas o desactivadas) como el resultado del diálogo del
 * sistema cuando deniega (antes salía una alerta sin ningún sitio a donde ir).
 *
 * El botón "Abrir ajustes" no llama a nada de plataforma aquí: HomeScreen intercepta
 * OnOpenNotificationsSettingsClick para llamar a abrirAjustesNotificaciones() en su propio
 * lambda, igual que hace con el requester del permiso, sin pasar por el canal de efectos.
 */
@Composable
fun NotificationsStatusDialog(
    activadas: Boolean,
    setEvent: (ListaCompraEvent) -> Unit = {}
) {
    Dialog(
        onDismissRequest = { setEvent(ListaCompraEvent.OnCloseNotificationsStatusDialog) },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.border(2.dp, PrimaryBlue, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.notification_blank),
                    contentDescription = null,
                    modifier = Modifier.size(70.dp)
                )

                val title = if (activadas) {
                    stringResource(Res.string.notifications_status_dialog_enabled_title)
                } else {
                    stringResource(Res.string.notifications_permission_denied_title)
                }
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                val message = if (activadas) {
                    stringResource(Res.string.notifications_status_dialog_enabled_message)
                } else {
                    stringResource(Res.string.notifications_permission_denied_message)
                }
                Text(
                    text = message,
                    fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                    fontSize = 15.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { setEvent(ListaCompraEvent.OnCloseNotificationsStatusDialog) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text(
                            text = stringResource(Res.string.notifications_status_dialog_close_button),
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                    Button(
                        onClick = { setEvent(ListaCompraEvent.OnOpenNotificationsSettingsClick) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = stringResource(Res.string.notifications_status_dialog_open_settings_button),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
