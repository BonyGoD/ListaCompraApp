package dev.bonygod.listacompra.notificaciones

import androidx.compose.runtime.Composable

@Composable
expect fun rememberNotificationPermissionRequester(onResult: (granted: Boolean) -> Unit): () -> Unit
