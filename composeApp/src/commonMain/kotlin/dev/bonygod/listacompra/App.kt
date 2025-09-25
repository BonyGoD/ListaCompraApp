package dev.bonygod.listacompra

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import dev.bonygod.listacompra.ui.screens.HomeScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        ScreenWrapper {
            HomeScreen()
        }
    }
}