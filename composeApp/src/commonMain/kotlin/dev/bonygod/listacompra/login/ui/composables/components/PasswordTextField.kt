package dev.bonygod.listacompra.login.ui.composables.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.login.ui.composables.interactions.LoginEvent
import dev.bonygod.listacompra.login.ui.composables.interactions.LoginState
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.close_eye_icon
import listacompra.composeapp.generated.resources.login_register_screen_password
import listacompra.composeapp.generated.resources.open_eye_icon
import listacompra.composeapp.generated.resources.pass_icon
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PasswordTextField(
    state: LoginState,
    setEvent: (LoginEvent) -> Unit
) {
    Column {
        Text(
            text = stringResource(Res.string.login_register_screen_password),
            fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 20.dp, top = 24.dp, bottom = 10.dp)
        )
        OutlinedTextField(
            value = state.loginUI.password,
            onValueChange = { setEvent(LoginEvent.OnPasswordChange(it)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            textStyle = TextStyle(fontSize = 20.sp),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.pass_icon),
                    contentDescription = "Email"
                )
            },
            trailingIcon = {
                IconButton(onClick = {  }) {
                    Icon(
                        painter = if (true) painterResource(Res.drawable.close_eye_icon) else painterResource(Res.drawable.open_eye_icon),
                        contentDescription = if (true) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        )
    }
}