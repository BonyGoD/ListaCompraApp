package dev.bonygod.listacompra.login.ui.composables.interactions

import dev.bonygod.listacompra.login.ui.composables.model.LoginUI

data class LoginState (
    val loginUI: LoginUI = LoginUI(),
    val loginError: String = ""
) {
    fun updateUsername(email: String): LoginState {
        return copy(loginUI = loginUI.copy(email = email))
    }
    fun updatePassword(password: String): LoginState {
        return copy(loginUI = loginUI.copy(password = password))
    }
    fun setLoginError(error: String): LoginState {
        return copy(loginError = error)
    }
    fun getUserData(): LoginUI {
        return loginUI
    }
}