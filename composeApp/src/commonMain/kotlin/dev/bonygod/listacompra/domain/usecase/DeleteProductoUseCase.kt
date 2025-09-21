package dev.bonygod.listacompra.domain.usecase

import dev.bonygod.listacompra.data.repository.ProductosRepository

class DeleteProductoUseCase(
    private val productosRepository: ProductosRepository
) {
    suspend operator fun invoke(id: String) {
        productosRepository.deleteProducto(id)
    }
}