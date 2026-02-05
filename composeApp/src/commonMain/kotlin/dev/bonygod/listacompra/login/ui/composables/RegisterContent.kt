package dev.bonygod.listacompra.login.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.googlesignin.kmp.ui.GoogleSignin
import dev.bonygod.listacompra.login.ui.composables.components.EmailTextField
import dev.bonygod.listacompra.login.ui.composables.components.GoogleSpacer
import dev.bonygod.listacompra.login.ui.composables.components.Header
import dev.bonygod.listacompra.login.ui.composables.components.PasswordTextField
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEvent
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthState
import dev.bonygod.listacompra.common.ui.theme.PrimaryBlue
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.back_button
import listacompra.composeapp.generated.resources.google_icon
import listacompra.composeapp.generated.resources.register_screen_google_register
import listacompra.composeapp.generated.resources.register_screen_name
import listacompra.composeapp.generated.resources.register_screen_or_register_with
import listacompra.composeapp.generated.resources.register_screen_register_button
import listacompra.composeapp.generated.resources.register_screen_subtitle
import listacompra.composeapp.generated.resources.register_screen_title
import listacompra.composeapp.generated.resources.user_icon
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val EMAIL_PADDING_TOP = 24.dp

@Composable
fun RegisterContent(
    state: AuthState,
    setEvent: (AuthEvent) -> Unit = {}
) {
    Icon(
        painterResource(Res.drawable.back_button),
        contentDescription = "Back Button",
        tint = Color.Black,
        modifier = Modifier
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(start = 20.dp)
            .clickable(
                onClick = { setEvent(AuthEvent.OnBackClick) }
            )
    )
    Column(modifier = Modifier.fillMaxSize().padding(top = 40.dp, bottom = 10.dp))
    {
        Header(
            stringResource(Res.string.register_screen_title),
            stringResource(Res.string.register_screen_subtitle)
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState()))
        {
            Text(
                text = stringResource(Res.string.register_screen_name),
                fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 20.dp, top = 50.dp, bottom = 10.dp)
            )
            OutlinedTextField(
                value = state.authUI.userName,
                onValueChange = { setEvent(AuthEvent.OnUserNameChange(it)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                textStyle = TextStyle(fontSize = 20.sp),
                shape = RoundedCornerShape(14.dp),
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.user_icon),
                        contentDescription = "userName"
                    )
                }
            )
            EmailTextField(EMAIL_PADDING_TOP, state = state, setEvent = setEvent)
            PasswordTextField(state, setEvent)
            PasswordTextField(state, setEvent, true)
            Button(
                onClick = {
                    setEvent(AuthEvent.OnRegisterClick)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 32.dp, end = 20.dp)
                    .height(56.dp),
                enabled = true,
                border = BorderStroke(1.dp, Color.Black),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                content = {
                    Text(
                        text = stringResource(Res.string.register_screen_register_button),
                        fontFamily = FontFamily(Font(Res.font.Inter_Italic)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            )
            GoogleSpacer(stringResource(Res.string.register_screen_or_register_with))
            GoogleSignin(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 30.dp)
                    .border(1.dp, Color(0xFF000000), RoundedCornerShape(30.dp))
                    .clip(shape = RoundedCornerShape(30.dp))
                    .height(50.dp),
                text = stringResource(Res.string.register_screen_google_register),
                textColor = Color.Black,
                icon = painterResource(Res.drawable.google_icon),
                onSuccess = { displayName, uid, email, photoUrl ->
                    setEvent(AuthEvent.OnGoogleSignInSuccess(uid, displayName, email))
                },
                onError = { errorMessage ->
                    setEvent(AuthEvent.OnGoogleSignInError(errorMessage))
                }
            )
        }
    }
}