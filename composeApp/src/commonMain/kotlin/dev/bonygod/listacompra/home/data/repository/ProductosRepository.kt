package dev.bonygod.listacompra.home.data.repository

import dev.bonygod.listacompra.home.data.datasource.ListaCompraDataSource
import dev.bonygod.listacompra.home.domain.model.Producto
import kotlinx.coroutines.flow.Flow

class ProductosRepository(
    private val listaCompraDS: ListaCompraDataSource
) {
    fun getProductos(): Flow<List<Producto>> {
        return listaCompraDS.getProductos()
    }

    suspend fun updateProducto(id: String, nombre: String, isImportant: Boolean) {
        listaCompraDS.updateProducto(id, nombre, isImportant)
    }

    suspend fun deleteProducto(id: String) {
        listaCompraDS.deleteProductos(id)
    }

    suspend fun deleteAllProductos() {
        listaCompraDS.deleteAllProductos()
    }

    suspend fun addProducto(producto: String) {
        listaCompraDS.addProducto(producto)
    }
}