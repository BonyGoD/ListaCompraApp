package dev.bonygod.listacompra.domain.usecase

import dev.bonygod.listacompra.data.repository.ProductosRepository

class AddProductoUseCase(
    private val productosRepository: ProductosRepository
) {
    suspend operator fun invoke(producto: String) {
        productosRepository.addProducto(producto)
    }
}