package dev.bonygod.listacompra.common.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bonygod.listacompra.common.ui.theme.PrimaryBlue
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun FullScreenLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(75.dp),
            color = PrimaryBlue
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FullScreenLoadingPreview() {
    FullScreenLoading()
}