package dev.bonygod.listacompra.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bonygod.listacompra.core.CustomFailures.LoginFailure
import dev.bonygod.listacompra.login.domain.mapper.toDomain
import dev.bonygod.listacompra.login.domain.usecase.ResetPasswordUseCase
import dev.bonygod.listacompra.login.domain.usecase.UserLoginUseCase
import dev.bonygod.listacompra.login.domain.usecase.UserRegisterUseCase
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEvent
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val userLoginUseCase: UserLoginUseCase,
    private val registerUseCase: UserRegisterUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun setState(reducer: AuthState.() -> AuthState) {
        _state.value = _state.value.reducer()
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.OnUserNameChange -> setState { updateUserName(event.value) }
            is AuthEvent.OnEmailChange -> setState { updateLoginEmail(event.value) }
            is AuthEvent.OnPasswordChange -> setState { updateLogimPassword(event.value) }
            is AuthEvent.OnConfirmPasswordChange -> setState { updateRegisterConfirmPassword(event.value) }
            is AuthEvent.OnEyePasswordClick -> setState { updateEyePassword() }
            is AuthEvent.OnEyeConfirmPasswordClick -> setState { updateEyeConfirmPassword() }
            is AuthEvent.OnResetPassword -> resetPassword(event.value)
            is AuthEvent.OnSignInClick -> logInWithEmail()
            is AuthEvent.OnRegisterClick -> registerUser()
            is AuthEvent.OnGoogleSignInSuccess -> { /* Handle Google Sign-In success if needed */ }
            is AuthEvent.OnGoogleSignInError -> setState { setAuthError(event.errorMessage) }
        }
    }

    private fun registerUser() {
        viewModelScope.launch {
            registerUseCase(state.value.getUserData().toDomain()).fold(
                onSuccess = {
                    //Effect Navigate
                },
                onFailure = { error ->
                    val errorMessage = (error as? LoginFailure)?.message ?: "Error desconocido"
                    setState { setAuthError(errorMessage) }
                }

            )
        }
    }

    private fun resetPassword(email: String) {
        viewModelScope.launch {
            resetPasswordUseCase(email).fold(
                onSuccess = {  },
                onFailure = { error ->
                    val errorMessage = (error as? LoginFailure)?.message ?: "Error desconocido"
                    setState { setAuthError(errorMessage) }
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
                    val errorMessage = (error as? LoginFailure)?.message ?: "Error desconocido"
                    setState { setAuthError(errorMessage) }
                }
            )
        }
    }
}