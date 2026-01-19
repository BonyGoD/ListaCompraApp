package dev.bonygod.listacompra.core.di

import dev.bonygod.listacompra.BuildConfig
import dev.bonygod.listacompra.core.analytics.AnalyticsService
import dev.bonygod.listacompra.core.navigation.Navigator
import dev.bonygod.listacompra.core.network.NetworkProvider
import dev.bonygod.listacompra.home.data.datasource.ListaCompraDataSource
import dev.bonygod.listacompra.home.data.repository.ProductosRepository
import dev.bonygod.listacompra.home.domain.usecase.AddProductoUseCase
import dev.bonygod.listacompra.home.domain.usecase.DeleteAllProductosUseCase
import dev.bonygod.listacompra.home.domain.usecase.DeleteProductoUseCase
import dev.bonygod.listacompra.home.domain.usecase.GetProductosUseCase
import dev.bonygod.listacompra.home.domain.usecase.UpdateProductoUseCase
import dev.bonygod.listacompra.home.ui.ListaCompraViewModel
import dev.bonygod.listacompra.login.data.datasource.UsersDataSource
import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.login.domain.usecase.GetUserUseCase
import dev.bonygod.listacompra.login.domain.usecase.LogOutUseCase
import dev.bonygod.listacompra.login.domain.usecase.ResetPasswordUseCase
import dev.bonygod.listacompra.login.domain.usecase.GoogleRegisterUserUseCase
import dev.bonygod.listacompra.login.domain.usecase.UserLoginUseCase
import dev.bonygod.listacompra.login.domain.usecase.UserRegisterUseCase
import dev.bonygod.listacompra.login.ui.AuthViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single { Navigator() }
    single { NetworkProvider().provideFirebaseClient() }
    single { NetworkProvider().provideAnalytics() }
    single { NetworkProvider().provideAuth() }
    single { AnalyticsService(get()) }
    single { ListaCompraDataSource(get()) }
    single { UsersDataSource(get(), get()) }
    single { ProductosRepository(get()) }
    single { UserRepository(get()) }
    single<String>(named("API_KEY")) { BuildConfig.FIREBASE_API_KEY }
    single<String>(named("CLIENT_ID")) { BuildConfig.CLIENT_ID }
}

val viewModelsModule = module {
    viewModelOf(::ListaCompraViewModel)
    viewModelOf(::AuthViewModel)
}

val dataModule = module {
    single { GetProductosUseCase(get()) }
    single { DeleteProductoUseCase(get()) }
    single { DeleteAllProductosUseCase(get()) }
    single { UpdateProductoUseCase(get()) }
    single { AddProductoUseCase(get()) }
    single { GetUserUseCase(get()) }
    single { UserLoginUseCase(get()) }
    single { LogOutUseCase(get()) }
    single { ResetPasswordUseCase(get()) }
    single { UserRegisterUseCase(get()) }
    single { GoogleRegisterUserUseCase(get()) }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, viewModelsModule, dataModule)
    }
}