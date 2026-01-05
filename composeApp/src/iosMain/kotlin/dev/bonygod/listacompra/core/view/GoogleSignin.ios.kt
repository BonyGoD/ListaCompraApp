package dev.bonygod.listacompra.core.view

import androidx.compose.runtime.Composable
import dev.bonygod.listacompra.core.network.GoogleAuthHelper
import dev.bonygod.listacompra.login.ui.components.GoogleButton

@Composable
actual fun GoogleSignin(navigateToWellcome: () -> Unit) {
    val googleAuthHelper = GoogleAuthHelper()

    GoogleButton(
        googleAuthHelper,
        navigateToWellcome
    )
}

