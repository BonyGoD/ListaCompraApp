package dev.bonygod.listacompra

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.bonygod.listacompra.core.navigation.NavigationWrapper

@Composable
fun App() {
    val snackbarHostState = remember { SnackbarHostState() }
    MaterialTheme {
        ScreenWrapper {
            NavigationWrapper(snackbarHostState)
        }
    }
}