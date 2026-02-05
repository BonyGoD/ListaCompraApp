package dev.bonygod.listacompra.login.ui.composables.interactions

import dev.bonygod.listacompra.core.navigation.Routes

sealed class AuthEffect {
    data class NavigateTo(val route: Routes) : AuthEffect()
    data class ShowError(val message: String) : AuthEffect()
    data object DismissDialog : AuthEffect()
    data class ShowInterstitialAndNavigate(val userId: String) : AuthEffect()
}