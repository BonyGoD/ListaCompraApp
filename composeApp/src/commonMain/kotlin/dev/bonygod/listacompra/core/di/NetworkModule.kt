package dev.bonygod.listacompra.core.di

import dev.bonygod.crashlytics.kmp.core.CrashReporter
import dev.bonygod.crashlytics.kmp.core.CrashlyticsKMP
import dev.bonygod.listacompra.BuildConfig
import dev.bonygod.listacompra.common.ui.state.SharedState
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
import dev.bonygod.listacompra.login.domain.usecase.AddSharedListUseCase
import dev.bonygod.listacompra.login.domain.usecase.DeleteAccountUseCase
import dev.bonygod.listacompra.login.domain.usecase.DeleteNotificationUseCase
import dev.bonygod.listacompra.login.domain.usecase.GetNotificationsUseCase
import dev.bonygod.listacompra.login.domain.usecase.GetUserUseCase
import dev.bonygod.listacompra.login.domain.usecase.GoogleRegisterUserUseCase
import dev.bonygod.listacompra.login.domain.usecase.LogOutUseCase
import dev.bonygod.listacompra.login.domain.usecase.ResetPasswordUseCase
import dev.bonygod.listacompra.login.domain.usecase.ShareListaCompraUseCase
import dev.bonygod.listacompra.login.domain.usecase.UserLoginUseCase
import dev.bonygod.listacompra.login.domain.usecase.UserRegisterUseCase
import dev.bonygod.listacompra.login.ui.AuthViewModel
import dev.bonygod.listacompra.mislistas.domain.usecase.AddNewListaUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.GetListasUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.RenameListaUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.SetDefaultListaUseCase
import dev.bonygod.listacompra.mislistas.ui.MisListasViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    // ⚠️ Koin cachea esta instancia en la primera resolución (single): CrashlyticsKMP.initialize()
    // debe ejecutarse ANTES de initKoin() en ambas plataformas (ListaCompraApp.onCreate / el
    // `configure` de MainViewController). Si se invirtiera el orden, quedaría cacheado para
    // siempre el NoOpCrashReporter previo a initialize() y la app dejaría de reportar en silencio.
    single<CrashReporter> { CrashlyticsKMP.reporter }
    single { Navigator(get()) }
    single { NetworkProvider().provideFirebaseClient() }
    single { NetworkProvider().provideAnalytics() }
    single { NetworkProvider().provideAuth() }
    single { AnalyticsService(get()) }
    single { ListaCompraDataSource(get(), get()) }
    single { UsersDataSource(get(), get()) }
    single { ProductosRepository(get()) }
    single { UserRepository(get(), get()) }
    single<String>(named("API_KEY")) { BuildConfig.FIREBASE_API_KEY }
    single<String>(named("CLIENT_ID")) { BuildConfig.CLIENT_ID }
}

val viewModelsModule = module {
    viewModelOf(::ListaCompraViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::MisListasViewModel)
}

val dataModule = module {
    single { GetProductosUseCase(get()) }
    single { DeleteProductoUseCase(get()) }
    single { DeleteAllProductosUseCase(get()) }
    single { UpdateProductoUseCase(get()) }
    single { AddProductoUseCase(get()) }
    single { GetUserUseCase(get()) }
    single { UserLoginUseCase(get()) }
    single { LogOutUseCase(get(), get()) }
    single { ResetPasswordUseCase(get()) }
    single { UserRegisterUseCase(get()) }
    single { GoogleRegisterUserUseCase(get()) }
    single { GetNotificationsUseCase(get()) }
    single { ShareListaCompraUseCase(get()) }
    single { AddSharedListUseCase(get()) }
    single { DeleteNotificationUseCase(get()) }
    single { DeleteAccountUseCase(get()) }
    single { SharedState() }
    single { GetListasUseCase(get()) }
    single { SetDefaultListaUseCase(get()) }
    single { RenameListaUseCase(get()) }
    single { AddNewListaUseCase(get()) }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, viewModelsModule, dataModule)
    }
}