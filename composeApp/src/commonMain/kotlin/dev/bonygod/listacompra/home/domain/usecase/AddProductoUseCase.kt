package dev.bonygod.listacompra.home.domain.usecase

import dev.bonygod.listacompra.home.data.repository.ProductosRepository

class AddProductoUseCase(
    private val productosRepository: ProductosRepository
) {
    suspend operator fun invoke(producto: String) {
        productosRepository.addProducto(producto)
    }
}