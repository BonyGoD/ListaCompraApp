package dev.bonygod.listacompra.home.ui.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.login.ui.composables.model.NotificationsUI
import dev.bonygod.listacompra.common.ui.theme.PrimaryBlue
import dev.bonygod.listacompra.common.ui.theme.SecondaryBlue
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowNotificationsBottomSheet(
    state: ListaCompraState,
    onEvent: (ListaCompraEvent) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Text(
            text = "Notificaciones",
            fontSize = 25.sp,
            fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 16.dp, start = 10.dp)
        )
        LazyColumn {
            items(state.notifications.size) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .border(1.dp, PrimaryBlue, RoundedCornerShape(15.dp))
                        .background(SecondaryBlue, RoundedCornerShape(15.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row {
                            Text(
                                text = state.notifications[item].nombre,
                                fontSize = 18.sp,
                                fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = " te ha compartido su lista",
                                fontSize = 18.sp,
                                fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                                fontWeight = FontWeight.Normal
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = { onEvent(ListaCompraEvent.OnAcceptSharedList(state.notifications[item].listaId)) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlue
                                )
                            ) {
                                Text("Aceptar")
                            }
                            Button(
                                onClick = { onEvent(ListaCompraEvent.OnCancelSharedList(state.notifications[item].listaId)) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray
                                ),
                                modifier = Modifier.padding(start = 10.dp)
                            ) {
                                Text("Cancelar")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ShowNotificationsBottomSheetPreview() {
    ShowNotificationsBottomSheet(
        state = ListaCompraState(
            notifications = listOf(
                NotificationsUI(
                    nombre = "Juan",
                    email = "juan@email.com",
                    listaId = "lista123"
                ),
                NotificationsUI(
                    nombre = "Ana",
                    email = "ana@email.com",
                    listaId = "lista456"
                )
            )
            // ...otros campos de ListaCompraState si son obligatorios, inicialízalos aquí...
        ),
        onEvent = {},
        onDismiss = {}
    )
}