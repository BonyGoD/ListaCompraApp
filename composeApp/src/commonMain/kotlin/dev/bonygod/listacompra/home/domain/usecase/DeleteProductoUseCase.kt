package dev.bonygod.listacompra.home.domain.usecase

class DeleteProductoUseCase(
    private val productosRepository: dev.bonygod.listacompra.home.data.repository.ProductosRepository
) {
    suspend operator fun invoke(id: String) {
        productosRepository.deleteProducto(id)
    }
}