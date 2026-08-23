package dev.bonygod.listacompra.alexa.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.alexa.ui.composables.interactions.AlexaEvent
import dev.bonygod.listacompra.alexa.ui.composables.interactions.AlexaState
import dev.bonygod.listacompra.common.ui.theme.PrimaryBlue
import dev.bonygod.listacompra.common.ui.theme.SecondaryBlue
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.alexa_screen_title
import listacompra.composeapp.generated.resources.back_button
import listacompra.composeapp.generated.resources.mislistas_back_description
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlexaContent(
    state: AlexaState,
    onEvent: (AlexaEvent) -> Unit
) {
    Scaffold(
        containerColor = SecondaryBlue,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SecondaryBlue),
                title = {
                    Text(
                        text = stringResource(Res.string.alexa_screen_title),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(AlexaEvent.GoBack) }) {
                        Icon(
                            painter = painterResource(Res.drawable.back_button),
                            contentDescription = stringResource(Res.string.mislistas_back_description),
                            tint = PrimaryBlue
                        )
                    }
                }
            )
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

            // Con scroll: el contenido crece con el número de listas del usuario, y la
            // tarjeta de frases ya ocupa media pantalla por sí sola. En un móvil pequeño,
            // o con el tamaño de fuente del sistema subido, se sale sin esto.
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    // Sin vincular hay una sola tarjeta pequeña, y arriba del todo deja
                    // media pantalla vacía debajo. Centrada parece puesta a propósito.
                    // Vinculado hay dos tarjetas que ya llenan, y ahí manda el orden de
                    // lectura: configuración arriba. Como el contenido puede pasarse de
                    // alto —muchas listas, o el tamaño de fuente del sistema subido—, el
                    // scroll sigue puesto y el centrado solo aplica mientras quepa.
                    verticalArrangement = if (state.alexaVinculada) {
                        Arrangement.Top
                    } else {
                        Arrangement.Center
                    }
                ) {
                    AlexaSection(state = state, onEvent = onEvent)
                }
            }
        }
    }
}
