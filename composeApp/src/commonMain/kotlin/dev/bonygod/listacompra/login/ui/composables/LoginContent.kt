package dev.bonygod.listacompra.login.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.googlesignin.kmp.ui.GoogleSignin
import dev.bonygod.listacompra.login.ui.composables.components.EmailTextField
import dev.bonygod.listacompra.login.ui.composables.components.GoogleSpacer
import dev.bonygod.listacompra.login.ui.composables.components.Header
import dev.bonygod.listacompra.login.ui.composables.components.NoAccountText
import dev.bonygod.listacompra.login.ui.composables.components.PasswordTextField
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEvent
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthState
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.google_icon
import listacompra.composeapp.generated.resources.login_screen_forgot_password
import listacompra.composeapp.generated.resources.login_screen_google_access
import listacompra.composeapp.generated.resources.login_screen_login_button
import listacompra.composeapp.generated.resources.login_screen_or_login_with
import listacompra.composeapp.generated.resources.login_screen_subtitle
import listacompra.composeapp.generated.resources.login_screen_title
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

private val EMAIL_FIELD_PADDING_TOP = 50.dp

@Composable
fun LoginContent(
    state: AuthState,
    setEvent: (AuthEvent) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Header(
            stringResource(Res.string.login_screen_title),
            stringResource(Res.string.login_screen_subtitle),
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState()))
        {
            EmailTextField(EMAIL_FIELD_PADDING_TOP, state, setEvent)
            PasswordTextField(state, setEvent)
            Text(
                text = stringResource(Res.string.login_screen_forgot_password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 7.dp)
                    .clickable(
                        onClick = {
                            setEvent(AuthEvent.OnResetPassword(state.authUI.email))
                        }
                    )
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 32.dp, end = 20.dp)
                    .height(56.dp),
                enabled = true,
                border = BorderStroke(1.dp, Color.Black),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A86C2)),
                content = {
                    Text(
                        text = stringResource(Res.string.login_screen_login_button),
                        fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                },
                onClick = {
                    setEvent(AuthEvent.OnSignInClick)
                }
            )
            GoogleSpacer(stringResource(Res.string.login_screen_or_login_with))
            GoogleSignin(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, bottom = 30.dp, top = 30.dp)
                    .border(1.dp, Color(0xFF000000), RoundedCornerShape(30.dp))
                    .clip(shape = RoundedCornerShape(30.dp))
                    .height(50.dp),
                text = stringResource(Res.string.login_screen_google_access),
                textColor = Color.Black,
                icon = painterResource(Res.drawable.google_icon),
                onSuccess = { displayName, uid, email, photoUrl ->
                    setEvent(AuthEvent.OnGoogleSignInSuccess)
                },
                onError = { errorMessage ->
                    setEvent(AuthEvent.OnGoogleSignInError(errorMessage))
                }
            )
            NoAccountText(setEvent)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthContentPreview() {
    //LoginContent()
}