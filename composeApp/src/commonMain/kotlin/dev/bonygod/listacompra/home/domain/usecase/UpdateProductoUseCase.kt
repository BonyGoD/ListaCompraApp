package dev.bonygod.listacompra.home.domain.usecase

import dev.bonygod.listacompra.home.data.repository.ProductosRepository

class UpdateProductoUseCase(
    private val productosRepository: ProductosRepository
) {
    suspend operator fun invoke(id: String, nombre: String, isImportant: Boolean) {
        productosRepository.updateProducto(id, nombre, isImportant)
    }
}