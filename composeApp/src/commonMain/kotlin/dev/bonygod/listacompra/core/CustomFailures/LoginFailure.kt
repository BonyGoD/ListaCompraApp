package dev.bonygod.listacompra.core.CustomFailures

sealed class LoginFailure(message: String) : Exception(message) {
    class IncorrectEmail : LoginFailure("El correo electrónico no es válido")
    class UserNotFound : LoginFailure("Usuario o contraseña inválidos")
    class PasswordsNotEqual : LoginFailure("Las contraseñas no coinciden")
    class IncorrectPassword : LoginFailure("La contraseña debe tener al menos 6 caracteres")
    class EmailInUse : LoginFailure("El correo electrónico ya está en uso")
    class BlankPassword : LoginFailure("Debes introducir una contraseña")
    class BlankUserName : LoginFailure("Debes introducir un nombre de usuario")
    class UnknownError : LoginFailure("Uknown login error")
}