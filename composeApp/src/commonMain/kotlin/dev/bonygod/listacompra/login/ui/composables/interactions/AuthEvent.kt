package dev.bonygod.listacompra.login.ui.composables.interactions

sealed class AuthEvent {
    data class OnUserNameChange(val value: String): AuthEvent()
    data class OnEmailChange(val value: String): AuthEvent()
    data class OnPasswordChange(val value: String): AuthEvent()
    data class OnConfirmPasswordChange(val value: String): AuthEvent()
    data object OnEyePasswordClick: AuthEvent()
    data object OnEyeConfirmPasswordClick: AuthEvent()
    data class OnResetPassword(val value: String): AuthEvent()
    data object OnSignInClick: AuthEvent()
    data object OnRegisterClick: AuthEvent()
    data class OnGoogleSignInSuccess(val uid: String, val displayName: String, val email: String): AuthEvent()
    data class OnGoogleSignInError(val errorMessage: String): AuthEvent()
    data object OnNavigateToRegister: AuthEvent()
    data object ShowLoading: AuthEvent()
    data object DismissDialog: AuthEvent()
    data object OnNavigateToForgotPassword: AuthEvent()
}