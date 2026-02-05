package dev.bonygod.listacompra.home.domain.usecase

import dev.bonygod.listacompra.home.data.repository.ProductosRepository

class AddProductoUseCase(
    private val productosRepo: ProductosRepository
) {
    suspend operator fun invoke(listaId: String, producto: String) {
        productosRepo.addProducto(listaId, producto)
    }
}