package dev.bonygod.listacompra.alexa.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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
import dev.bonygod.listacompra.mislistas.ui.model.ListaInfoUI
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.mislistas_alexa_active_badge
import listacompra.composeapp.generated.resources.mislistas_alexa_anonymous_message
import listacompra.composeapp.generated.resources.mislistas_alexa_default_hint
import listacompra.composeapp.generated.resources.mislistas_alexa_howto_title
import listacompra.composeapp.generated.resources.mislistas_alexa_howto_open
import listacompra.composeapp.generated.resources.mislistas_alexa_howto_open_hint
import listacompra.composeapp.generated.resources.mislistas_alexa_howto_oneshot
import listacompra.composeapp.generated.resources.mislistas_alexa_howto_oneshot_hint
import listacompra.composeapp.generated.resources.mislistas_alexa_howto_warning
import listacompra.composeapp.generated.resources.mislistas_alexa_link_account_button
import listacompra.composeapp.generated.resources.mislistas_alexa_not_linked_message
import listacompra.composeapp.generated.resources.mislistas_alexa_selector_title
import org.jetbrains.compose.resources.stringResource

/**
 * Contenido de la pantalla Alexa. Tres estados, según fase 5 del plan de Alexa:
 * - Anónimo: no hay selector, solo aviso + enlace al flujo de vincular cuenta ya
 *   existente (reutiliza LinkAccountDialog vía PendingHomeAction.requestLinkAccount()).
 * - Con cuenta, sin vincular (alexaVinculada == false): la vinculación se hace desde
 *   la app de Alexa, no desde aquí.
 * - Vinculado: selector de a qué lista escribe Alexa, alimentado por `getListas()`.
 *
 * Mudado tal cual desde MisListasScreen (antes AlexaSection dentro de MisListas):
 * mismo composable, mismos textos, solo cambia el estado/evento que consume.
 */
@Composable
fun AlexaSection(
    state: AlexaState,
    onEvent: (AlexaEvent) -> Unit
) {
    // Sin título propio: cuando esto era un bloque dentro de MisListas hacía falta para
    // separarlo de las listas. Ahora la pantalla entera es Alexa y lo pone la barra
    // superior, así que repetirlo aquí era decir "Alexa" dos veces seguidas.
    Column(modifier = Modifier.fillMaxWidth()) {
        when {
            state.isAnonymous -> AlexaCard {
                Text(
                    text = stringResource(Res.string.mislistas_alexa_anonymous_message),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                // El botón dice "Crear cuenta" pero abre el diálogo de vinculación
                // (linkAccountWithEmailUseCase) a propósito, no Routes.Register: es el
                // único camino que crea la cuenta sin tirar las listas del anónimo.
                Button(
                    onClick = { onEvent(AlexaEvent.OnLinkAccountForAlexaClick) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = stringResource(Res.string.mislistas_alexa_link_account_button),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            !state.alexaVinculada -> AlexaCard {
                Text(
                    text = stringResource(Res.string.mislistas_alexa_not_linked_message),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Dos tarjetas separadas y no una: son dos cosas distintas. Arriba se
            // configura (a qué lista escribe), abajo se aprende (qué hay que decir).
            else -> {
                AlexaCard {
                    Text(
                        text = stringResource(Res.string.mislistas_alexa_selector_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(Res.string.mislistas_alexa_default_hint),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    state.listas.forEach { lista ->
                        val isActive = if (state.listaAlexa.isNotBlank()) {
                            lista.id == state.listaAlexa
                        } else {
                            lista.isDefault
                        }
                        AlexaListaRow(
                            lista = lista,
                            isActive = isActive,
                            onSelect = { onEvent(AlexaEvent.SelectListaAlexa(lista.id)) }
                        )
                    }
                }

                AlexaCard { AlexaHowTo() }
            }
        }
    }
}

/** Tarjeta blanca con borde, el contenedor visual de la pantalla. */
@Composable
private fun AlexaCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(width = 1.dp, color = Color.LightGray, shape = shape)
            .background(color = Color.White, shape = shape)
            .padding(16.dp),
        content = content
    )
}

/**
 * Qué frases funcionan. No es adorno: el nombre de invocación hay que decirlo tal cual,
 * y si se dice de otra forma Alexa resuelve por su lista nativa **sin dar error**, así que
 * el producto acaba en un sitio que el usuario no ve y parece que la app ha fallado.
 * Este es el sitio donde más gente lo va a leer: acaban de vincular la cuenta aquí.
 */
@Composable
private fun AlexaHowTo() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.mislistas_alexa_howto_title),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(10.dp))

        AlexaFrase(
            frase = stringResource(Res.string.mislistas_alexa_howto_open),
            pista = stringResource(Res.string.mislistas_alexa_howto_open_hint)
        )
        Spacer(modifier = Modifier.height(8.dp))
        AlexaFrase(
            frase = stringResource(Res.string.mislistas_alexa_howto_oneshot),
            pista = stringResource(Res.string.mislistas_alexa_howto_oneshot_hint)
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(Res.string.mislistas_alexa_howto_warning),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun AlexaFrase(frase: String, pista: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFF2F5F8), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = frase, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = pista, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun AlexaListaRow(
    lista: ListaInfoUI,
    isActive: Boolean,
    onSelect: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) PrimaryBlue else Color.LightGray,
                shape = shape
            )
            .clickable { onSelect() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lista.nombre,
                fontSize = 14.sp,
                color = Color.Black
            )
            if (isActive) {
                Text(
                    text = stringResource(Res.string.mislistas_alexa_active_badge),
                    fontSize = 11.sp,
                    color = PrimaryBlue
                )
            }
        }
    }
}
