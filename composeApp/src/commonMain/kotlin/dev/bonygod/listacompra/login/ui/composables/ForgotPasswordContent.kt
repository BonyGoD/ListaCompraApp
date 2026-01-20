package dev.bonygod.listacompra.login.ui.composables

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.login.ui.composables.components.EmailTextField
import dev.bonygod.listacompra.login.ui.composables.components.Header
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEvent
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthState
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.login_screen_login_button
import listacompra.composeapp.generated.resources.login_screen_subtitle
import listacompra.composeapp.generated.resources.login_screen_title
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview


private val EMAIL_FIELD_PADDING_TOP = 50.dp

@Composable
fun ForgotPasswordContent(
    state: AuthState,
    setEvent: (AuthEvent) -> Unit = {}
){
    Column(modifier = Modifier.fillMaxSize()) {
        Header(
            stringResource(Res.string.login_screen_title),
            stringResource(Res.string.login_screen_subtitle),
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState()))
        {
            EmailTextField(EMAIL_FIELD_PADDING_TOP, state, setEvent)
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
                    setEvent(AuthEvent.OnResetPassword(state.authUI.email))
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ForgotPasswordPreview() {
    ForgotPasswordContent(AuthState())
}
