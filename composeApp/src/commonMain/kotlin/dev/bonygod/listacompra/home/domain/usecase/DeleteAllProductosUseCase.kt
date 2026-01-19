package dev.bonygod.listacompra.home.domain.usecase

import dev.bonygod.listacompra.home.data.repository.ProductosRepository

class DeleteAllProductosUseCase(
    private val productosRepo: ProductosRepository
) {
    suspend operator fun invoke(listaId: String) {
        productosRepo.deleteAllProductos(listaId)
    }
}
