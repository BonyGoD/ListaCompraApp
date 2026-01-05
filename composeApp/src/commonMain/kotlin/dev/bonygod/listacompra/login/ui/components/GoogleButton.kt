package dev.bonygod.listacompra.login.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.core.network.GoogleAuthHelper
import kotlinx.coroutines.launch
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.google_icon
import org.jetbrains.compose.resources.painterResource

@Composable
fun GoogleButton(googleAuthHelper: GoogleAuthHelper, navigateToWellcome: () -> Unit) {

    val error = rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
//    val userViewModel = koinViewModel<UserViewModel>()

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .border(1.dp, Color(0xFF000000), RoundedCornerShape(30.dp))
            .clip(shape = RoundedCornerShape(30.dp))
            .height(50.dp),
        onClick = {
            scope.launch {
                googleAuthHelper.signInWithGoogle(
                    onSuccess = { userName, userUid, tokenId, mail, photo ->
//                        val userDb = createUserDb(userUid, userName, mail, tokenId)
//                        userViewModel.insertUser(userDb)
                        navigateToWellcome()
                    },
                    onError = { errorMsg ->
                        error.value = errorMsg
                    })
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color(0xFF000000)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.google_icon),
                    contentDescription = "Google",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }
            Text(
                "Acceder con Google",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}