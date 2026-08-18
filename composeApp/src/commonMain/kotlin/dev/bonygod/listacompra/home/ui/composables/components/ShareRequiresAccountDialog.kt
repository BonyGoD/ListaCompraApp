package dev.bonygod.listacompra.home.ui.composables.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import listacompra.composeapp.generated.resources.app_icon
import listacompra.composeapp.generated.resources.share_requires_account_dialog_cancel_button
import listacompra.composeapp.generated.resources.share_requires_account_dialog_confirm_button
import listacompra.composeapp.generated.resources.share_requires_account_dialog_message
import listacompra.composeapp.generated.resources.share_requires_account_dialog_title
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShareRequiresAccountDialog(
    setEvent: (ListaCompraEvent) -> Unit = {}
) {
    Dialog(
        onDismissRequest = { setEvent(ListaCompraEvent.OnShareAccountRequiredCancel) },
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
                    painter = painterResource(Res.drawable.app_icon),
                    contentDescription = null,
                    modifier = Modifier.size(70.dp)
                )

                Text(
                    text = stringResource(Res.string.share_requires_account_dialog_title),
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = stringResource(Res.string.share_requires_account_dialog_message),
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
                        onClick = { setEvent(ListaCompraEvent.OnShareAccountRequiredCancel) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text(
                            text = stringResource(Res.string.share_requires_account_dialog_cancel_button),
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = { setEvent(ListaCompraEvent.OnShareAccountRequiredConfirm) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(text = stringResource(Res.string.share_requires_account_dialog_confirm_button))
                    }
                }
            }
        }
    }
}
