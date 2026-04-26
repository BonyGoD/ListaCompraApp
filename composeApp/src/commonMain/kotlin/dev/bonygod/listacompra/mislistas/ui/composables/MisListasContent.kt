package dev.bonygod.listacompra.mislistas.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.common.ui.theme.PrimaryBlue
import dev.bonygod.listacompra.common.ui.theme.SecondaryBlue
import dev.bonygod.listacompra.mislistas.ui.composables.interactions.MisListasEvent
import dev.bonygod.listacompra.mislistas.ui.composables.interactions.MisListasState
import dev.bonygod.listacompra.mislistas.ui.model.ListaInfoUI
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.back_button
import listacompra.composeapp.generated.resources.listas
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisListasContent(
    state: MisListasState,
    onEvent: (MisListasEvent) -> Unit
) {
    // Rename dialog
    if (state.renameDialogListaId != null) {
        NombreDialog(
            title = "Renombrar lista",
            initialNombre = state.renameDialogCurrentNombre,
            confirmLabel = "Renombrar",
            onConfirm = { nombre -> onEvent(MisListasEvent.ConfirmRename(state.renameDialogListaId, nombre)) },
            onDismiss = { onEvent(MisListasEvent.DismissDialog) }
        )
    }

    // Create dialog
    if (state.showCreateDialog) {
        NombreDialog(
            title = "Nueva lista",
            initialNombre = "",
            confirmLabel = "Crear",
            onConfirm = { nombre -> onEvent(MisListasEvent.ConfirmCreate(nombre)) },
            onDismiss = { onEvent(MisListasEvent.DismissDialog) }
        )
    }

    Scaffold(
        containerColor = SecondaryBlue,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SecondaryBlue),
                title = {
                    Text(
                        text = "Mis listas",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(MisListasEvent.GoBack) }) {
                        Icon(
                            painter = painterResource(Res.drawable.back_button),
                            contentDescription = "Volver",
                            tint = PrimaryBlue
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(MisListasEvent.ShowCreateDialog) },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.error, color = Color.Red, fontSize = 16.sp)
                }
            }

            state.listas.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No tienes listas disponibles", color = Color.Gray)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(state.listas) { lista ->
                        ListaItem(
                            lista = lista,
                            onSelect = { onEvent(MisListasEvent.SelectLista(lista.id)) },
                            onRename = { onEvent(MisListasEvent.ShowRenameDialog(lista.id, lista.nombre)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NombreDialog(
    title: String,
    initialNombre: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf(initialNombre) }
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (nombre.isNotBlank()) onConfirm(nombre.trim())
                })
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (nombre.isNotBlank()) onConfirm(nombre.trim()) },
                enabled = nombre.isNotBlank()
            ) {
                Text(confirmLabel, color = PrimaryBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}

@Composable
private fun ListaItem(
    lista: ListaInfoUI,
    onSelect: () -> Unit,
    onRename: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor = if (lista.isDefault) PrimaryBlue else Color.LightGray
    val borderWidth = if (lista.isDefault) 2.dp else 1.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .background(
                color = if (lista.isDefault) SecondaryBlue else Color.White,
                shape = shape
            )
            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.listas),
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lista.nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            if (lista.isDefault) {
                Text(
                    text = "Predeterminada",
                    fontSize = 12.sp,
                    color = PrimaryBlue
                )
            }
        }
        // Rename button
        IconButton(onClick = onRename) {
            Text(text = "✏️", fontSize = 18.sp)
        }
    }
}