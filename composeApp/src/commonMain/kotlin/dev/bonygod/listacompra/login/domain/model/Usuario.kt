package dev.bonygod.listacompra.login.domain.model

data class Usuario (
    val uid: String,
    val nombre: String,
    val email: String,
    val apiKey: String,
    val listas: List<String>
)