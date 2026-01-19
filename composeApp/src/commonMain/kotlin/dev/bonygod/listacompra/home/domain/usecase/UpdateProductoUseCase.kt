package dev.bonygod.listacompra.home.domain.usecase

import dev.bonygod.listacompra.home.data.repository.ProductosRepository

class UpdateProductoUseCase(
    private val productosRepo: ProductosRepository
) {
    suspend operator fun invoke(listaId: String, id: String, nombre: String, isImportant: Boolean) {
        productosRepo.updateProducto(listaId, id, nombre, isImportant)
    }
}