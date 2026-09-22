package dev.bonygod.listacompra.notificaciones.data

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

/**
 * El motor se nombra en cada plataforma, en vez de dejar que `HttpClient { }` lo busque
 * solo. Sin motor explícito, Ktor lo localiza en tiempo de ejecución escaneando
 * `META-INF/services`, una relación que R8 no ve: en release puede llevarse por delante
 * la clase del motor y el cliente deja de construirse con "Failed to find HTTP client
 * engine implementation in the classpath".
 *
 * Y ese fallo sería invisible, porque quien llama envuelve el aviso en un try/catch para
 * que un problema de red no estropee el compartir: en debug funcionaría y en producción
 * no llegaría ninguna push, sin un solo error a la vista.
 */
internal expect fun crearHttpClient(configuracion: HttpClientConfig<*>.() -> Unit): HttpClient
