package dev.bonygod.listacompra.home.domain.usecase

class DeleteAllProductosUseCase(
    private val productosRepository: dev.bonygod.listacompra.home.data.repository.ProductosRepository
) {
    suspend operator fun invoke() {
        productosRepository.deleteAllProductos()
    }
}
