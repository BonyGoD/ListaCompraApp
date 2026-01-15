package dev.bonygod.listacompra.login.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import listacompra.composeapp.generated.resources.Inter_Italic
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.google_icon
import listacompra.composeapp.generated.resources.register_screen_confirm_password
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
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RegisterContent() {
    Column(modifier = Modifier.fillMaxSize())
    {
        Header(
            stringResource(Res.string.register_screen_title),
            stringResource(Res.string.register_screen_subtitle),
            50.dp
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
                value = "",
                onValueChange = { },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                textStyle = TextStyle(fontSize = 20.sp),
                shape = RoundedCornerShape(14.dp),
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.user_icon),
                        contentDescription = "Email"
                    )
                }
            )
//            EmailTextField(paddingTop = 24, state = state, onEvent = onEvent)
//            PasswordTextField(state)
//            PasswordTextField(state, )
            Button(
                onClick = { },
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
                    // Handle successful sign-in
                },
                onError = { errorMessage ->
                    // Handle sign-in error
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterContentPreview() {
    RegisterContent()
}