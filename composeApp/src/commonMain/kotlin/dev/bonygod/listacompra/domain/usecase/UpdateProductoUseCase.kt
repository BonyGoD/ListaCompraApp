package dev.bonygod.listacompra.domain.usecase

import dev.bonygod.listacompra.data.repository.ProductosRepository

class UpdateProductoUseCase(
    private val productosRepository: ProductosRepository
) {
    suspend operator fun invoke(id: String, nombre: String) {
        productosRepository.updateProducto(id, nombre)
    }
}