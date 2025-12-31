package dev.bonygod.listacompra.home.domain.usecase

import dev.bonygod.listacompra.home.ui.mapper.toListaCompraUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetProductosUseCase(
    private val productosRepository: dev.bonygod.listacompra.home.data.repository.ProductosRepository
) {
    operator fun invoke(): Flow<dev.bonygod.listacompra.home.ui.model.ListaCompraUI> {
        return productosRepository.getProductos()
            .map { productos -> productos.toListaCompraUI() }
    }
}
