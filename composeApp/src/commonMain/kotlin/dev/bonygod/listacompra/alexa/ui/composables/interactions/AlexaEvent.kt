package dev.bonygod.listacompra.alexa.ui.composables.interactions

sealed class AlexaEvent {
    data object GoBack : AlexaEvent()

    /** Elegir a qué lista escribe Alexa. Solo actualiza `listaAlexa`. */
    data class SelectListaAlexa(val listaId: String) : AlexaEvent()

    /** Usuario anónimo pulsando "vincular cuenta" en la pantalla Alexa. */
    data object OnLinkAccountForAlexaClick : AlexaEvent()
}
