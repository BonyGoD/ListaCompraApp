package dev.bonygod.listacompra.home.ui.composables.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEvent
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.app_icon
import listacompra.composeapp.generated.resources.login_screen_dialog_button
import listacompra.composeapp.generated.resources.login_screen_dialog_subtitle
import listacompra.composeapp.generated.resources.login_screen_dialog_title
import listacompra.composeapp.generated.resources.mail_icon
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ShareListaCompraDialog(
    state: ListaCompraState,
    setEvent: (ListaCompraEvent) -> Unit = {}
) {
    Dialog(
        onDismissRequest = { setEvent(ListaCompraEvent.DismissCustomDialog) },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.border(2.dp, Color(0xFF3A86C2), RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(Res.drawable.app_icon),
                    contentDescription = "Confirmación",
                    modifier = Modifier.size(70.dp)
                )
                Spacer(modifier = Modifier.weight(0.3f))
                Text(
                    text = "Comparte tu lista de la compra",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.weight(0.3f))
                Text(
                    text = "Introduce el correo electrónico de la persona con la que quieres compartir tu lista",
                    fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                    fontSize = 15.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.weight(0.7f))
                OutlinedTextField(
                    value = state.shareTextField,
                    onValueChange = { setEvent(ListaCompraEvent.OnShareTextFieldChange(it)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    textStyle = TextStyle(fontSize = 20.sp),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.mail_icon),
                            contentDescription = "Email"
                        )
                    }
                )
                Button(
                    onClick = {
                        setEvent(ListaCompraEvent.ShareList(state.shareTextField.text))
                        setEvent(ListaCompraEvent.DismissCustomDialog)
                    },
                    modifier = Modifier.padding(bottom = 20.dp, top = 20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A86C2))
                ) {
                    Text(text = stringResource(Res.string.login_screen_dialog_button))
                }
            }
        }
    }
}

@Preview
@Composable
fun ShareListaCompraDialogPreview() {

}