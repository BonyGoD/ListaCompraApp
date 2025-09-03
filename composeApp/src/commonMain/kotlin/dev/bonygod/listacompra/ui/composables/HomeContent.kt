package dev.bonygod.listacompra.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeContent() {
    Button(onClick = { /*no-op*/ }) {

    }
    FlowColumn(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(50.dp)){
            Text(
                modifier = Modifier.padding(10.dp),
                text = "Tomates",
                fontSize = 24.sp
            )
        }
    }
}