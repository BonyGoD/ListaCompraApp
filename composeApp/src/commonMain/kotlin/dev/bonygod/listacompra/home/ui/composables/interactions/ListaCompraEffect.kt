package dev.bonygod.listacompra.home.ui.composables.interactions

import dev.bonygod.listacompra.core.navigation.Routes

sealed class ListaCompraEffect {
    data class NavigateTo(val route: Routes) : ListaCompraEffect()
    data class ShowError(val message: String) : ListaCompraEffect()
    data object DismissDialog : ListaCompraEffect()
}