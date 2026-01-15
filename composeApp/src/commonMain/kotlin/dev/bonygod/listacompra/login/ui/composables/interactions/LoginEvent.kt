package dev.bonygod.listacompra.login.ui.composables.interactions

sealed class LoginEvent {
    data class OnEmailChange(val value: String): LoginEvent()
    data class OnPasswordChange(val value: String): LoginEvent()
    data class OnResetPassword(val value: String): LoginEvent()
    data object OnSignInClick: LoginEvent()
}