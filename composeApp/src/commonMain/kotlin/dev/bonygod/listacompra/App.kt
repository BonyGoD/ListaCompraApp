package dev.bonygod.listacompra

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.bonygod.listacompra.login.ui.screens.LoginScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val snackbarHostState = remember { SnackbarHostState() }
    MaterialTheme {
        ScreenWrapper {
            LoginScreen(snackbarHostState)
        }
    }
}