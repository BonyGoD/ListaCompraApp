package dev.bonygod.listacompra.home.data.repository

import dev.bonygod.listacompra.home.data.datasource.ListaCompraDataSource
import dev.bonygod.listacompra.home.domain.model.Producto
import kotlinx.coroutines.flow.Flow

class ProductosRepository(
    private val listaCompraDS: ListaCompraDataSource
) {
    fun getProductos(listaId: String): Flow<List<Producto>> {
        return listaCompraDS.getProductos(listaId)
    }

    suspend fun updateProducto(listaId: String, id: String, nombre: String, isImportant: Boolean) {
        listaCompraDS.updateProducto(listaId, id, nombre, isImportant)
    }

    suspend fun deleteProducto(listaId: String, id: String) {
        listaCompraDS.deleteProductos(listaId,id)
    }

    suspend fun deleteAllProductos(listaId: String, ) {
        listaCompraDS.deleteAllProductos(listaId)
    }

    suspend fun addProducto(listaId: String, producto: String) {
        listaCompraDS.addProducto(listaId, producto)
    }
}