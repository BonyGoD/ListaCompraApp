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

    suspend fun deleteProducto(id: String) {
        listaCompraDataService.deleteProductos(id)
    }

    suspend fun deleteAllProductos() {
        listaCompraDataService.deleteAllProductos()
    }
}