package dev.bonygod.listacompra.home.domain.usecase

class UpdateProductoUseCase(
    private val productosRepository: dev.bonygod.listacompra.home.data.repository.ProductosRepository
) {
    suspend operator fun invoke(id: String, nombre: String, isImportant: Boolean) {
        productosRepository.updateProducto(id, nombre, isImportant)
    }
}