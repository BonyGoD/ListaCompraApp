package dev.bonygod.listacompra.login.data.model

data class UserResponse(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val apiKey: String = "",
    val listas: List<String> = listOf()
)