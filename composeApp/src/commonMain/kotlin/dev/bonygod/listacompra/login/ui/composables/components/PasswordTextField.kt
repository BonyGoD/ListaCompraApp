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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEvent
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthState
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.close_eye_icon
import listacompra.composeapp.generated.resources.lock_icon
import listacompra.composeapp.generated.resources.login_register_screen_password
import listacompra.composeapp.generated.resources.open_eye_icon
import listacompra.composeapp.generated.resources.pass_icon
import listacompra.composeapp.generated.resources.register_screen_confirm_password
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PasswordTextField(
    state: AuthState,
    setEvent: (AuthEvent) -> Unit,
    confirmPassword: Boolean = false
) {
    val labelPasswordText = if (confirmPassword) {
        stringResource(Res.string.register_screen_confirm_password)
    } else {
        stringResource(Res.string.login_register_screen_password)
    }

    val passwordValue = if (confirmPassword) {
        state.authUI.confirmPassword
    } else {
        state.authUI.password
    }

    val iconPassword = if (confirmPassword) {
        painterResource(Res.drawable.lock_icon)
    } else {
        painterResource(Res.drawable.pass_icon)
    }

    val eyeOpen = if (confirmPassword) {
        state.eyeConfirmPassword
    } else {
        state.eyePasswordOpen
    }

    Column {
        Text(
            text = labelPasswordText,
            fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 20.dp, top = 24.dp, bottom = 10.dp)
        )
        OutlinedTextField(
            value = passwordValue,
            onValueChange = {
                if(!confirmPassword) {
                    setEvent(AuthEvent.OnPasswordChange(it))
                } else {
                    setEvent(AuthEvent.OnConfirmPasswordChange(it))
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            textStyle = TextStyle(fontSize = 20.sp),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            visualTransformation = if (eyeOpen) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    painter = iconPassword,
                    contentDescription = "Email"
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    if(confirmPassword) {
                        setEvent(AuthEvent.OnEyeConfirmPasswordClick)
                    } else {
                        setEvent(AuthEvent.OnEyePasswordClick)
                    }
                }) {
                    Icon(
                        painter = if (eyeOpen) painterResource(Res.drawable.close_eye_icon) else painterResource(Res.drawable.open_eye_icon),
                        contentDescription = if (eyeOpen) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        )
    }
}