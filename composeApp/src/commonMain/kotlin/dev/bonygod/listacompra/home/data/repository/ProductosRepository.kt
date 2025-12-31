package dev.bonygod.listacompra.home.data.repository

import kotlinx.coroutines.flow.Flow

class ProductosRepository(
    private val listaCompraDataService: dev.bonygod.listacompra.home.data.network.ListaCompraDataService
) {
    fun getProductos(): Flow<List<dev.bonygod.listacompra.home.domain.model.Producto>> {
        return listaCompraDataService.getProductos()
    }

    suspend fun updateProducto(id: String, nombre: String, isImportant: Boolean) {
        listaCompraDataService.updateProducto(id, nombre, isImportant)
    }

    suspend fun deleteProducto(id: String) {
        listaCompraDataService.deleteProductos(id)
    }

    suspend fun deleteAllProductos() {
        listaCompraDataService.deleteAllProductos()
    }

    suspend fun addProducto(producto: String) {
        listaCompraDataService.addProducto(producto)
    }
}