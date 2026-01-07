package dev.bonygod.listacompra.core.view

import androidx.compose.runtime.Composable
import dev.bonygod.listacompra.core.network.GoogleAuthHelper
import dev.bonygod.listacompra.login.ui.components.GoogleButton

@Composable
actual fun GoogleSignin(
    onSuccess: (displayName: String, uid: String, email: String, photoUrl: String) -> Unit,
    onError: (errorMessage: String) -> Unit
) {
    val googleAuthHelper = GoogleAuthHelper()

    GoogleButton(
        googleAuthHelper = googleAuthHelper,
        onSuccess = onSuccess,
        onError = onError
    )
}

