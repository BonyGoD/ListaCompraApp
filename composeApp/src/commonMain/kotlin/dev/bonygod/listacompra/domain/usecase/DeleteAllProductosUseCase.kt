package dev.bonygod.listacompra.domain.usecase

import dev.bonygod.listacompra.data.repository.ProductosRepository

class DeleteAllProductosUseCase(
    private val productosRepository: ProductosRepository
) {
    suspend operator fun invoke() {
        productosRepository.deleteAllProductos()
    }
}
