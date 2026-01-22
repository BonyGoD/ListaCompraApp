package dev.bonygod.listacompra.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bonygod.listacompra.common.ui.state.SharedState
import dev.bonygod.listacompra.core.CustomFailures.LoginFailure
import dev.bonygod.listacompra.core.navigation.Navigator
import dev.bonygod.listacompra.core.navigation.Routes
import dev.bonygod.listacompra.login.domain.mapper.toDomain
import dev.bonygod.listacompra.login.domain.usecase.GoogleRegisterUserUseCase
import dev.bonygod.listacompra.login.domain.usecase.ResetPasswordUseCase
import dev.bonygod.listacompra.login.domain.usecase.UserLoginUseCase
import dev.bonygod.listacompra.login.domain.usecase.UserRegisterUseCase
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEffect
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthEvent
import dev.bonygod.listacompra.login.ui.composables.interactions.AuthState
import dev.bonygod.listacompra.login.ui.composables.mapper.toUI
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class AuthViewModel(
    private val navigator: Navigator,
    private val sharedState: SharedState,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val userLoginUseCase: UserLoginUseCase,
    private val registerUseCase: UserRegisterUseCase,
    private val googleRegisterUserUseCase: GoogleRegisterUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    private val _effect = MutableSharedFlow<AuthEffect>(replay = 1)
    val effect: SharedFlow<AuthEffect> = _effect.asSharedFlow()

    private var lastResetRequestTime: Long = 0L

    fun setState(reducer: AuthState.() -> AuthState) {
        _state.value = _state.value.reducer()
    }

    private fun setEffect(effect: AuthEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.OnUserNameChange -> setState { updateUserName(event.value) }
            is AuthEvent.OnEmailChange -> setState { updateLoginEmail(event.value) }
            is AuthEvent.OnPasswordChange -> setState { updateLoginPassword(event.value) }
            is AuthEvent.OnConfirmPasswordChange -> setState { updateRegisterConfirmPassword(event.value) }
            is AuthEvent.OnEyePasswordClick -> setState { updateEyePassword() }
            is AuthEvent.OnEyeConfirmPasswordClick -> setState { updateEyeConfirmPassword() }
            is AuthEvent.OnResetPassword -> resetPassword(event.value)
            is AuthEvent.OnSignInClick -> logInWithEmail()
            is AuthEvent.OnRegisterClick -> registerUser()
            is AuthEvent.OnGoogleSignInSuccess -> createUserListAndNavigate(
                event.uid,
                event.displayName,
                event.email
            )

            is AuthEvent.OnGoogleSignInError -> setEffect(AuthEffect.ShowError(event.errorMessage))
            is AuthEvent.OnNavigateToRegister -> navigator.navigateTo(Routes.Register)
            is AuthEvent.ShowLoading -> sharedState.showLoading(true)
            is AuthEvent.DismissDialog -> setState { showDialog(false) }
            is AuthEvent.OnNavigateToForgotPassword -> navigator.navigateTo(Routes.ForgotPassword)
        }
    }

    private fun createUserListAndNavigate(uid: String, displayName: String, email: String) {
        viewModelScope.launch {
            googleRegisterUserUseCase(uid, displayName, email).fold(
                onSuccess = { usuario ->
                    setState { setUserData(usuario.toUI()) }
                    navigator.clearAndNavigateTo(Routes.Home(usuario.uid))
                },
                onFailure = { error ->
                    val errorMessage = (error as? LoginFailure)?.message ?: "Error desconocido"
                    setEffect(AuthEffect.ShowError(errorMessage))
                }
            )
        }
    }

    private fun registerUser() {
        viewModelScope.launch {
            registerUseCase(state.value.getUserData().toDomain()).fold(
                onSuccess = { usuario ->
                    navigator.navigateTo(Routes.Home(usuario.uid))
                },
                onFailure = { error ->
                    val errorMessage = (error as? LoginFailure)?.message ?: "Error desconocido"
                    setEffect(AuthEffect.ShowError(errorMessage))
                }
            )
        }
    }

    private fun resetPassword(email: String) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        if (currentTime - lastResetRequestTime < 5.minutes.inWholeMilliseconds) {
            setEffect(AuthEffect.ShowError("Debes esperar 5 minutos para volver a intentarlo."))
            return
        }

        viewModelScope.launch {
            resetPasswordUseCase(email).fold(
                onSuccess = {
                    lastResetRequestTime = Clock.System.now().toEpochMilliseconds()
                    setState { updateLoginEmail("") }
                    setState { showDialog(true) }
                },
                onFailure = { error ->
                    val errorMessage = (error as? LoginFailure)?.message ?: "Error desconocido"
                    setEffect(AuthEffect.ShowError(errorMessage))
                }
            )
        }
    }

    private fun logInWithEmail() {
        viewModelScope.launch {
            val user = state.value.getUserData()
            userLoginUseCase(user.email, user.password).fold(
                onSuccess = { usuario ->
                    navigator.clearAndNavigateTo(Routes.Home(usuario.uid))
                },
                onFailure = { error ->
                    val errorMessage = (error as? LoginFailure)?.message ?: "Error desconocido"
                    setEffect(AuthEffect.ShowError(errorMessage))
                }
            )
        }
    }
}