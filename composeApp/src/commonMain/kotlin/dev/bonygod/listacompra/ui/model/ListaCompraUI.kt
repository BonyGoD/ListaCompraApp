package dev.bonygod.listacompra.ui.model

data class ListaCompraUI(
    val productos: List<ProductoUI> = emptyList()
) {
    fun updateUnidadesProducto(id: String, unidades: Int): ListaCompraUI {
        val productIndex = productos.indexOfFirst { it.id == id }
        if(productIndex == -1) return this

        val producto = productos[productIndex]
        val updatedProduct = producto.updateUnidades(producto, unidades)
        val unidadesActualizadas = productos.toMutableList().apply { set(productIndex, updatedProduct) }

        return copy(productos = unidadesActualizadas)
    }
}