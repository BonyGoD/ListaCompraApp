package dev.bonygod.listacompra.core.di

import dev.bonygod.listacompra.core.network.NetworkProvider
import dev.bonygod.listacompra.data.network.ListaCompraDataService
import dev.bonygod.listacompra.data.repository.ProductosRepository
import dev.bonygod.listacompra.domain.usecase.DeleteAllProductosUseCase
import dev.bonygod.listacompra.domain.usecase.DeleteProductoUseCase
import dev.bonygod.listacompra.domain.usecase.GetProductosUseCase
import dev.bonygod.listacompra.ui.ListaCompraViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single { NetworkProvider().provideFirebaseClient() }
    single { ListaCompraDataService(get()) }
    single { ProductosRepository(get()) }
}

val viewModelsModule = module {
    viewModelOf(::ListaCompraViewModel)
}

val dataModule = module {
    single { GetProductosUseCase(get()) }
    single { DeleteProductoUseCase(get()) }
    single { DeleteAllProductosUseCase(get()) }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, viewModelsModule, dataModule)
    }
}