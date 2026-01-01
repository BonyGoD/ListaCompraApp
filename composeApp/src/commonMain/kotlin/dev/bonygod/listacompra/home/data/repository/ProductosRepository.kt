package dev.bonygod.listacompra.home.data.repository

import dev.bonygod.listacompra.home.data.datasource.ListaCompraDataSource
import kotlinx.coroutines.flow.Flow

class ProductosRepository(
    private val listaCompraDataSource: ListaCompraDataSource
) {
    fun getProductos(): Flow<List<dev.bonygod.listacompra.home.domain.model.Producto>> {
        return listaCompraDataSource.getProductos()
    }

    suspend fun updateProducto(id: String, nombre: String, isImportant: Boolean) {
        listaCompraDataSource.updateProducto(id, nombre, isImportant)
    }

    suspend fun deleteProducto(id: String) {
        listaCompraDataSource.deleteProductos(id)
    }

    suspend fun deleteAllProductos() {
        listaCompraDataSource.deleteAllProductos()
    }

    suspend fun addProducto(producto: String) {
        listaCompraDataSource.addProducto(producto)
    }
}