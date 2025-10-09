package dev.bonygod.listacompra.data.repository

import dev.bonygod.listacompra.data.network.ListaCompraDataService
import dev.bonygod.listacompra.domain.model.Producto
import kotlinx.coroutines.flow.Flow

class ProductosRepository(
    private val listaCompraDataService: ListaCompraDataService
) {
    fun getProductos(): Flow<List<Producto>> {
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