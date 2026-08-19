package dev.bonygod.listacompra.home.ui.composables.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.bonygod.listacompra.common.ui.theme.PrimaryBlue
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraState
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.link_account_dialog_cancel_button
import listacompra.composeapp.generated.resources.link_account_dialog_confirm_button
import listacompra.composeapp.generated.resources.link_account_dialog_subtitle
import listacompra.composeapp.generated.resources.link_account_dialog_title
import listacompra.composeapp.generated.resources.lock_icon
import listacompra.composeapp.generated.resources.login_register_screen_email
import listacompra.composeapp.generated.resources.login_register_screen_password
import listacompra.composeapp.generated.resources.mail_icon
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LinkAccountDialog(
    state: ListaCompraState,
    setEvent: (ListaCompraEvent) -> Unit = {}
) {
    Dialog(
        onDismissRequest = { setEvent(ListaCompraEvent.OnDismissLinkAccountDialog) },
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
                Text(
                    text = stringResource(Res.string.link_account_dialog_title),
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = stringResource(Res.string.link_account_dialog_subtitle),
                    fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                    fontSize = 15.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                OutlinedTextField(
                    value = state.linkEmail,
                    onValueChange = { setEvent(ListaCompraEvent.OnLinkEmailChange(it)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    textStyle = TextStyle(fontSize = 18.sp),
                    shape = RoundedCornerShape(14.dp),
                    label = { Text(stringResource(Res.string.login_register_screen_email)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.mail_icon),
                            contentDescription = null
                        )
                    }
                )

                OutlinedTextField(
                    value = state.linkPassword,
                    onValueChange = { setEvent(ListaCompraEvent.OnLinkPasswordChange(it)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    textStyle = TextStyle(fontSize = 18.sp),
                    shape = RoundedCornerShape(14.dp),
                    label = { Text(stringResource(Res.string.login_register_screen_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.lock_icon),
                            contentDescription = null
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { setEvent(ListaCompraEvent.OnDismissLinkAccountDialog) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text(
                            text = stringResource(Res.string.link_account_dialog_cancel_button),
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                    Button(
                        onClick = { setEvent(ListaCompraEvent.OnLinkAccountConfirm) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = stringResource(Res.string.link_account_dialog_confirm_button),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
