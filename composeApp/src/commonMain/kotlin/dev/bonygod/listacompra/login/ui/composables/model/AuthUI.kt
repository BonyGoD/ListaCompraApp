package dev.bonygod.listacompra.login.ui.composables.model

data class AuthUI(
    val userName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)