package dev.bonygod.listacompra.core.view

import androidx.compose.runtime.Composable

@Composable
expect fun GoogleSignin(
    onSuccess: (displayName: String, uid: String, email: String, photoUrl: String) -> Unit,
    onError: (errorMessage: String) -> Unit
)
