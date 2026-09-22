package dev.bonygod.listacompra.notificaciones.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.common.ui.theme.PrimaryBlue
import dev.bonygod.listacompra.common.ui.theme.SecondaryBlue
import dev.bonygod.listacompra.login.ui.composables.model.NotificationsUI
import dev.bonygod.listacompra.notificaciones.ui.composables.interactions.NotificacionesEvent
import dev.bonygod.listacompra.notificaciones.ui.composables.interactions.NotificacionesState
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.back_button
import listacompra.composeapp.generated.resources.mislistas_back_description
import listacompra.composeapp.generated.resources.notifications_sheet_accept_button
import listacompra.composeapp.generated.resources.notifications_sheet_cancel_button
import listacompra.composeapp.generated.resources.notifications_sheet_empty_message
import listacompra.composeapp.generated.resources.notifications_sheet_shared_suffix
import listacompra.composeapp.generated.resources.notifications_sheet_title
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesContent(
    state: NotificacionesState,
    onEvent: (NotificacionesEvent) -> Unit
) {
    Scaffold(
        containerColor = SecondaryBlue,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SecondaryBlue),
                title = {
                    Text(
                        text = stringResource(Res.string.notifications_sheet_title),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(NotificacionesEvent.GoBack) }) {
                        Icon(
                            painter = painterResource(Res.drawable.back_button),
                            contentDescription = stringResource(Res.string.mislistas_back_description),
                            tint = PrimaryBlue
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.error, color = Color.Red, fontSize = 16.sp)
                }
            }

            state.notifications.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(Res.string.notifications_sheet_empty_message), color = Color.Gray)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    items(state.notifications) { notification ->
                        NotificationItem(notification = notification, onEvent = onEvent)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: NotificationsUI,
    onEvent: (NotificacionesEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border(1.dp, PrimaryBlue, RoundedCornerShape(15.dp))
            .background(SecondaryBlue, RoundedCornerShape(15.dp))
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = notification.nombre,
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(Res.string.notifications_sheet_shared_suffix),
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        onEvent(NotificacionesEvent.Accept(notification.listaId, notification.listaNombre))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(stringResource(Res.string.notifications_sheet_accept_button))
                }
                Button(
                    onClick = { onEvent(NotificacionesEvent.Reject(notification.listaId)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    modifier = Modifier.padding(start = 10.dp)
                ) {
                    Text(stringResource(Res.string.notifications_sheet_cancel_button))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificacionesContentPreview() {
    NotificacionesContent(
        state = NotificacionesState(
            isLoading = false,
            notifications = listOf(
                NotificationsUI(nombre = "Juan", email = "juan@email.com", listaId = "lista123"),
                NotificationsUI(nombre = "Ana", email = "ana@email.com", listaId = "lista456")
            )
        ),
        onEvent = {}
    )
}
