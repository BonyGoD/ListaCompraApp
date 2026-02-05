package dev.bonygod.listacompra.home.domain.usecase

import dev.bonygod.listacompra.home.data.repository.ProductosRepository
import dev.bonygod.listacompra.home.ui.mapper.toListaCompraUI
import dev.bonygod.listacompra.home.ui.model.ListaCompraUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetProductosUseCase(
    private val productosRepo: ProductosRepository,
) {
    operator fun invoke(listaId: String): Flow<ListaCompraUI> {
        return productosRepo.getProductos(listaId)
            .map { productos -> productos.toListaCompraUI() }
    }
}
