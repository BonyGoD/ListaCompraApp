package dev.bonygod.listacompra.alexa.ui.composables.interactions

import dev.bonygod.listacompra.mislistas.ui.model.ListaInfoUI

/**
 * Estado de la pantalla Alexa (fase 5 del plan de Alexa). Antes vivía dentro de
 * MisListasState como una sección más; se independiza porque la vinculación de
 * Alexa no es una propiedad de las listas, es configuración de la integración.
 */
data class AlexaState(
    val listas: List<ListaInfoUI> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isAnonymous: Boolean = false,
    val alexaVinculada: Boolean = false,
    val listaAlexa: String = ""
)
