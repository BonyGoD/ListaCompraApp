package dev.bonygod.listacompra.core.di

import dev.bonygod.listacompra.ui.ListaCompraViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {

}

val viewModelsModule = module {
    viewModelOf(::ListaCompraViewModel)
}

val dataModule = module {

}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, viewModelsModule, dataModule)
    }
}