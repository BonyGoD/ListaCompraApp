package dev.bonygod.listacompra.home.domain.usecase

import dev.bonygod.listacompra.home.data.repository.ProductosRepository

class DeleteAllProductosUseCase(
    private val productosRepository: ProductosRepository
) {
    suspend operator fun invoke() {
        productosRepository.deleteAllProductos()
    }
}
