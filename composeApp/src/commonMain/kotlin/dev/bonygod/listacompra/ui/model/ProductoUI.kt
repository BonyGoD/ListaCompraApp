package dev.bonygod.listacompra.ui.model

data class ProductoUI(
    val id: String = "",
    val nombre: String = "",
    val fecha: String = "",
    val unidades: Int = 0
) {
    fun updateUnidades(producto: ProductoUI, unidades: Int): ProductoUI {
        return producto.copy(unidades = unidades)
    }
}
