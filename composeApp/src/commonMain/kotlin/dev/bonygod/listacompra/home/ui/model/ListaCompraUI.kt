package dev.bonygod.listacompra.home.ui.model

data class ListaCompraUI(
    val productos: List<ProductoUI> = emptyList(),
    val user: UserUI? = null
)