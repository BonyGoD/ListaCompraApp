package dev.bonygod.listacompra.notificaciones.data

import dev.gitlive.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val BASE_URL = "https://api-devware.vercel.app"
private const val REQUEST_TIMEOUT_MS = 5_000L

@Serializable
private data class CompartirListaBody(val email: String, val listaId: String)

class NotificacionesApi(private val auth: FirebaseAuth) {
    private val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
        }
    }

    suspend fun avisarListaCompartida(email: String, listaId: String) {
        val idToken = auth.currentUser?.getIdToken(false) ?: return
        val body = Json.encodeToString(
            CompartirListaBody.serializer(),
            CompartirListaBody(email = email, listaId = listaId)
        )
        client.post("$BASE_URL/notificaciones/lista-compartida") {
            header("Authorization", "Bearer $idToken")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}
