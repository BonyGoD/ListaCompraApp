package dev.bonygod.listacompra.ui.composables.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.ui.composables.interactions.ListaCompraEvent
import org.jetbrains.compose.ui.tooling.preview.Preview

// NOTE: Using ExperimentalMaterial3Api because ModalBottomSheet and related components are currently only available as experimental in Material3.
// Be aware that future Compose updates may introduce breaking changes. Consider updating to stable APIs when available.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductBottomSheet(
    newProductText: String,
    onEvent: (ListaCompraEvent) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Agregar Producto",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = newProductText,
                onValueChange = { onEvent(ListaCompraEvent.UpdateNewProductText(it)) },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onEvent(ListaCompraEvent.AddProducto) },
                modifier = Modifier.fillMaxWidth(),
                enabled = newProductText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00913F)
                )
            ) {
                Text("Agregar")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}