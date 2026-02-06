package dev.bonygod.listacompra.login.ui.composables.model

data class AuthUI(
    val uid: String = "",
    val userName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val listas: List<String> = emptyList()
)