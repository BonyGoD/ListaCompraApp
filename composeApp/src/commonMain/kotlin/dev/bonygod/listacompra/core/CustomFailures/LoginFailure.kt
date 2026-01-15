package dev.bonygod.listacompra.core.CustomFailures

sealed class LoginFailure(message: String): Exception(message) {
    class IncorrectEmail : LoginFailure("El correo electrónico no es válido")
    class UserNotFound : LoginFailure("Usuario o contraseña inválidos")
    class UnknownError : LoginFailure("Uknown login error")
}