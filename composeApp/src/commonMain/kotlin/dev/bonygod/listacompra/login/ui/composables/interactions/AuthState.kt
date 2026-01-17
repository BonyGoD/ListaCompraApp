package dev.bonygod.listacompra.login.ui.composables.interactions

import dev.bonygod.listacompra.login.ui.composables.model.AuthUI

data class AuthState (
    val authUI: AuthUI = AuthUI(),
    val eyePasswordOpen: Boolean = false,
    val eyeConfirmPassword: Boolean = false
) {
    fun updateUserName(userName: String): AuthState {
        return copy(authUI = authUI.copy(userName = userName))
    }
    fun updateLoginEmail(email: String): AuthState {
        return copy(authUI = authUI.copy(email = email))
    }
    fun updateLogimPassword(password: String): AuthState {
        return copy(authUI = authUI.copy(password = password))
    }
    fun updateRegisterConfirmPassword(confirmPassword: String): AuthState {
        return copy(authUI = authUI.copy(confirmPassword = confirmPassword))
    }
    fun updateEyePassword(): AuthState {
        return copy(eyePasswordOpen = !eyePasswordOpen)
    }
    fun updateEyeConfirmPassword(): AuthState {
        return copy(eyeConfirmPassword = !eyeConfirmPassword)
    }
    fun getUserData(): AuthUI {
        return authUI
    }
}