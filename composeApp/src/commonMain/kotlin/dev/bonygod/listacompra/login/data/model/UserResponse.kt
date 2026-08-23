package dev.bonygod.listacompra.login.data.model

data class UserResponse(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val apiKey: String = "",
    val listas: List<String> = listOf(),
    // `listaAlexa` lo escribe la app (id de la lista donde escribe Alexa; si está vacío,
    // la API usa listas[0]). `alexaVinculada` lo escribe la API vía Admin SDK; la app solo lee.
    val listaAlexa: String = "",
    val alexaVinculada: Boolean = false
)