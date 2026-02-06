package dev.bonygod.listacompra.home.ui.model

data class ProductoUI(
    val id: String = "",
    val nombre: String = "",
    val fecha: String = "",
    val isImportant: Boolean = false,
    val unidades: Int = 0
)
