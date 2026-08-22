package dev.bonygod.listacompra.mislistas.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bonygod.listacompra.common.ui.theme.PrimaryBlue
import dev.bonygod.listacompra.mislistas.ui.composables.interactions.MisListasEvent
import dev.bonygod.listacompra.mislistas.ui.composables.interactions.MisListasState
import dev.bonygod.listacompra.mislistas.ui.model.ListaInfoUI
import listacompra.composeapp.generated.resources.Res
import listacompra.composeapp.generated.resources.mislistas_alexa_active_badge
import listacompra.composeapp.generated.resources.mislistas_alexa_anonymous_message
import listacompra.composeapp.generated.resources.mislistas_alexa_default_hint
import listacompra.composeapp.generated.resources.mislistas_alexa_link_account_button
import listacompra.composeapp.generated.resources.mislistas_alexa_not_linked_message
import listacompra.composeapp.generated.resources.mislistas_alexa_section_title
import listacompra.composeapp.generated.resources.mislistas_alexa_selector_title
import org.jetbrains.compose.resources.stringResource

/**
 * Sección "Alexa" de MisListas. Tres estados, según fase 5 del plan de Alexa:
 * - Anónimo: no hay selector, solo aviso + enlace al flujo de vincular cuenta ya
 *   existente (reutiliza LinkAccountDialog vía Routes.Home(openLinkAccount = true)).
 * - Con cuenta, sin vincular (alexaVinculada == false): la vinculación se hace desde
 *   la app de Alexa, no desde aquí.
 * - Vinculado: selector de a qué lista escribe Alexa, alimentado por las mismas
 *   `listas` que ya carga MisListasScreen.
 */
@Composable
fun AlexaSection(
    state: MisListasState,
    onEvent: (MisListasEvent) -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(width = 1.dp, color = Color.LightGray, shape = shape)
            .background(color = Color.White, shape = shape)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.mislistas_alexa_section_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))

        when {
            state.isAnonymous -> {
                Text(
                    text = stringResource(Res.string.mislistas_alexa_anonymous_message),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { onEvent(MisListasEvent.OnLinkAccountForAlexaClick) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.mislistas_alexa_link_account_button),
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            !state.alexaVinculada -> {
                Text(
                    text = stringResource(Res.string.mislistas_alexa_not_linked_message),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            else -> {
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
                        onSelect = { onEvent(MisListasEvent.SelectListaAlexa(lista.id)) }
                    )
                }
            }
        }
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
