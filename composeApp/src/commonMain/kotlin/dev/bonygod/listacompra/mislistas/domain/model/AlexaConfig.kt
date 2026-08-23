package dev.bonygod.listacompra.mislistas.domain.model

/**
 * Configuración de Alexa del usuario, leída de `usuarios/{uid}`.
 *
 * `listaAlexa` lo escribe la app: id de la lista donde Alexa añade productos. Si está
 * vacío, la API (fuera de este repo) cae en `listas[0]`, la lista predeterminada.
 * `alexaVinculada` lo escribe la API vía Admin SDK cuando el usuario completa el
 * account linking desde la app de Alexa; aquí solo se lee.
 */
data class AlexaConfig(
    val listaAlexa: String = "",
    val alexaVinculada: Boolean = false
)
