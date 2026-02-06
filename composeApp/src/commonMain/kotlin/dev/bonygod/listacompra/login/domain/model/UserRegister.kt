package dev.bonygod.listacompra.login.domain.model

data class UserRegister(
    val uid: String = "",
    val userName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)