package dev.bonygod.listacompra.home.domain.usecase

import dev.bonygod.listacompra.home.data.repository.ProductosRepository

class DeleteProductoUseCase(
    private val productosRepo: ProductosRepository
) {
    suspend operator fun invoke(listaId: String, id: String) {
        productosRepo.deleteProducto(listaId, id)
    }
}