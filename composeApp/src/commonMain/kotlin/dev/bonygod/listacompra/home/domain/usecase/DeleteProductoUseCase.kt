package dev.bonygod.listacompra.home.domain.usecase

import dev.bonygod.listacompra.home.data.repository.ProductosRepository

class DeleteProductoUseCase(
    private val productosRepository: ProductosRepository
) {
    suspend operator fun invoke(id: String) {
        productosRepository.deleteProducto(id)
    }
}