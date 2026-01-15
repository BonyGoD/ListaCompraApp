package dev.bonygod.listacompra.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bonygod.listacompra.core.CustomFailures.LoginFailure
import dev.bonygod.listacompra.login.domain.usecase.ResetPasswordUseCase
import dev.bonygod.listacompra.login.domain.usecase.UserLoginUseCase
import dev.bonygod.listacompra.login.ui.composables.interactions.LoginEvent
import dev.bonygod.listacompra.login.ui.composables.interactions.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val userLoginUseCase: UserLoginUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun setState(reducer: LoginState.() -> LoginState) {
        _state.value = _state.value.reducer()
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnEmailChange -> setState { updateUsername(event.value) }
            is LoginEvent.OnPasswordChange -> setState { updatePassword(event.value) }
            is LoginEvent.OnResetPassword -> resetPassword(event.value)
            is LoginEvent.OnSignInClick -> logInWithEmail()
        }
    }

    private fun resetPassword(email: String) {
        viewModelScope.launch {
            resetPasswordUseCase(email).fold(
                onSuccess = {  },
                onFailure = { error ->
                    when (error) {
                        is LoginFailure.IncorrectEmail -> setState { setLoginError(error.message.orEmpty()) }
                        is LoginFailure.UnknownError -> setState { setLoginError(error.message.orEmpty()) }
                        else -> setState { setLoginError(error.message ?: "Error desconocido") }
                    }
                }
            )
        }
    }

    private fun logInWithEmail() {
        viewModelScope.launch {
            val user = state.value.getUserData()
            userLoginUseCase(user.email, user.password).fold(
                onSuccess = {  },
                onFailure = { error ->
                    when (error) {
                        is LoginFailure.IncorrectEmail -> setState { setLoginError(error.message.orEmpty()) }
                        is LoginFailure.UserNotFound -> setState { setLoginError(error.message.orEmpty()) }
                        is LoginFailure.UnknownError -> setState { setLoginError(error.message.orEmpty()) }
                        else -> setState { setLoginError(error.message ?: "Error desconocido") }
                    }
                }
            )
        }
    }
}