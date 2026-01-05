package dev.bonygod.listacompra.core.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import dev.bonygod.listacompra.core.network.GoogleAuthHelper
import dev.bonygod.listacompra.login.ui.components.GoogleButton

@Composable
actual fun GoogleSignin(navigateToWellcome: () -> Unit) {
    val context = LocalContext.current
    val googleAuthHelper = GoogleAuthHelper(context, CredentialManager.create(context))

    GoogleButton(
        googleAuthHelper,
        navigateToWellcome
    )
}