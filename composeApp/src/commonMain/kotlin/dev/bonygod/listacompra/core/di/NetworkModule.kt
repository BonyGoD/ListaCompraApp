package dev.bonygod.listacompra.core.di

import dev.bonygod.listacompra.core.analytics.AnalyticsService
import dev.bonygod.listacompra.core.network.NetworkProvider
import dev.bonygod.listacompra.home.data.datasource.ListaCompraDataSource
import dev.bonygod.listacompra.home.data.repository.ProductosRepository
import dev.bonygod.listacompra.home.domain.usecase.AddProductoUseCase
import dev.bonygod.listacompra.home.domain.usecase.DeleteAllProductosUseCase
import dev.bonygod.listacompra.home.domain.usecase.DeleteProductoUseCase
import dev.bonygod.listacompra.home.domain.usecase.GetProductosUseCase
import dev.bonygod.listacompra.home.domain.usecase.UpdateProductoUseCase
import dev.bonygod.listacompra.home.ui.ListaCompraViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single { NetworkProvider().provideFirebaseClient() }
    single { NetworkProvider().provideAnalytics() }
    single { AnalyticsService(get()) }
    single { ListaCompraDataSource(get()) }
    single { ProductosRepository(get()) }
}

val viewModelsModule = module {
    viewModelOf(::ListaCompraViewModel)
}

val dataModule = module {
    single { GetProductosUseCase(get()) }
    single {
        DeleteProductoUseCase(
            get()
        )
    }
    single {
        DeleteAllProductosUseCase(
            get()
        )
    }
    single {
        UpdateProductoUseCase(
            get()
        )
    }
    single { AddProductoUseCase(get()) }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, viewModelsModule, dataModule)
    }
}