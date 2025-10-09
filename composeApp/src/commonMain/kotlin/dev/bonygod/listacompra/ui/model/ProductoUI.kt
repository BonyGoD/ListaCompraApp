package dev.bonygod.listacompra.ui.model

data class ProductoUI(
    val id: String = "",
    val nombre: String = "",
    val fecha: String = "",
    val isImportant: Boolean = false,
    val unidades: Int = 0
) {
    val isImportatProduct: Boolean get() = isImportant.not()
    fun updateUnidades(producto: ProductoUI, unidades: Int): ProductoUI {
        return copy(unidades = unidades)
    }
}
