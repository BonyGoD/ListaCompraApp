package dev.bonygod.listacompra.home.ui.composables.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraState

@Composable
fun TextFieldComponent(
    onEvent: (ListaCompraEvent) -> Unit,
    state: ListaCompraState,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = state.editingText,
            onValueChange = { onEvent(ListaCompraEvent.UpdateEditingText(it)) },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(fontSize = 24.sp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "✓",
            modifier = Modifier
                .clickable { onEvent(ListaCompraEvent.SaveEditedProduct) },
            fontSize = 24.sp,
            color = Color(0xFF00913F)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "✕",
            modifier = Modifier
                .clickable { onEvent(ListaCompraEvent.CancelEditing) },
            fontSize = 24.sp,
            color = Color.Red
        )
    }
}