package dev.bonygod.listacompra.home.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.googlesignin.kmp.ui.GoogleSignin
import dev.bonygod.listacompra.ScreenWrapper
import dev.bonygod.listacompra.home.ui.composables.components.AddProductBottomSheet
import dev.bonygod.listacompra.home.ui.composables.components.ConfirmDialog
import dev.bonygod.listacompra.home.ui.composables.components.ErrorAlert
import dev.bonygod.listacompra.home.ui.composables.components.SuccessAlert
import dev.bonygod.listacompra.home.ui.composables.components.TextComponent
import dev.bonygod.listacompra.home.ui.composables.components.TextFieldComponent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraEvent
import dev.bonygod.listacompra.home.ui.composables.interactions.ListaCompraState
import dev.bonygod.listacompra.home.ui.composables.preview.ListaCompraPreview
import dev.bonygod.listacompra.home.ui.model.ListaCompraUI
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.basura
import listacompra.composeapp.generated.resources.google_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HomeContent(
    data: ListaCompraUI,
    state: ListaCompraState,
    onEvent: (ListaCompraEvent) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 10.dp, top = 10.dp, bottom = 20.dp)
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 10.dp),
                    text = "Lista de la compra",
                    fontSize = 25.sp,
                    fontWeight = Bold
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable {
                            onEvent(ListaCompraEvent.ShowDialog(true))
                        }
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(Res.drawable.basura),
                        contentDescription = "Icono borrar todo",
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = "Borrar lista",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
            LazyColumn(modifier = Modifier.padding(10.dp)) {
                items(data.productos) { producto ->
                    if (state.editingProductId == producto.id) {
                        TextFieldComponent(
                            onEvent = onEvent,
                            state = state
                        )
                    } else {
                        TextComponent(
                            producto = producto,
                            onEvent = onEvent
                        )
                    }
                    Spacer(
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00913F)),
            content = {
                Text(
                    fontWeight = Bold,
                    fontSize = 15.sp,
                    text = "Agregar producto"
                )
            },
            onClick = {
                onEvent(ListaCompraEvent.ShowBottomSheet(true))
            }
        )
        GoogleSignin(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .border(1.dp, Color(0xFF000000), RoundedCornerShape(30.dp))
                .clip(shape = RoundedCornerShape(30.dp))
                .height(50.dp),
            text = "Login with google",
            textColor = Color.Black,
            icon = painterResource(Res.drawable.google_icon),
            onSuccess = { displayName, uid, email, photoUrl ->
                // Handle successful sign-in
            },
            onError = { errorMessage ->
                // Handle sign-in error
            }
        )
    }

    if (state.dialogState) {
        ConfirmDialog(
            title = "¿Estás seguro de borrar todos los productos?",
            message = "Esta acción no se puede deshacer.",
            onConfirm = { onEvent(ListaCompraEvent.ConfirmDelete) },
            onCancel = { onEvent(ListaCompraEvent.CancelDialog) }
        )
    }

    if (state.showErrorAlert) {
        ErrorAlert(
            title = state.errorAlertTitle,
            message = state.errorAlertMessage,
            onDismiss = { onEvent(ListaCompraEvent.HideErrorAlert) }
        )
    }

    if (state.showSuccessAlert) {
        SuccessAlert(
            title = state.successAlertTitle,
            message = state.successAlertMessage,
            onDismiss = { onEvent(ListaCompraEvent.HideSuccessAlert) }
        )
    }

    if (state.showBottomSheet) {
        AddProductBottomSheet(
            state = state,
            onEvent = onEvent,
            onDismiss = { onEvent(ListaCompraEvent.ShowBottomSheet(false)) }
        )
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    ScreenWrapper {
        HomeContent(
            data = ListaCompraPreview.ListaCompraUI,
            state = ListaCompraState(listaCompraUI = ListaCompraPreview.ListaCompraUI),
        )
    }
}