package dev.bonygod.listacompra.login.domain.model

data class UserRegister (
    val userName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)