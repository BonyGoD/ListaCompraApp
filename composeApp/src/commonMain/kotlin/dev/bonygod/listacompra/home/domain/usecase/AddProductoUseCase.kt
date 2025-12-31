package dev.bonygod.listacompra.home.domain.usecase

class AddProductoUseCase(
    private val productosRepository: dev.bonygod.listacompra.home.data.repository.ProductosRepository
) {
    suspend operator fun invoke(producto: String) {
        productosRepository.addProducto(producto)
    }
}