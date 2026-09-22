package dev.bonygod.listacompra.core.di

import dev.bonygod.admob.kmp.AdMobKMP
import dev.bonygod.admob.kmp.config.AdMobConfig
import dev.bonygod.crashlytics.kmp.core.CrashReporter
import dev.bonygod.crashlytics.kmp.core.CrashlyticsKMP
import dev.bonygod.listacompra.BuildConfig
import dev.bonygod.listacompra.alexa.ui.AlexaViewModel
import dev.bonygod.listacompra.common.ui.state.SharedState
import dev.bonygod.listacompra.core.analytics.AnalyticsService
import dev.bonygod.listacompra.core.navigation.Navigator
import dev.bonygod.listacompra.core.navigation.PendingHomeAction
import dev.bonygod.listacompra.core.navigation.PendingNotificationAction
import dev.bonygod.listacompra.core.network.NetworkProvider
import dev.bonygod.listacompra.core.preferences.PreferenciasLocales
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
import dev.bonygod.listacompra.login.domain.usecase.GuardarTokenPushUseCase
import dev.bonygod.listacompra.login.domain.usecase.IsAnonymousUserUseCase
import dev.bonygod.listacompra.login.domain.usecase.LinkAccountWithEmailUseCase
import dev.bonygod.listacompra.login.domain.usecase.LogOutUseCase
import dev.bonygod.listacompra.login.domain.usecase.ResetPasswordUseCase
import dev.bonygod.listacompra.login.domain.usecase.ResolveSessionUseCase
import dev.bonygod.listacompra.login.domain.usecase.ShareListaCompraUseCase
import dev.bonygod.listacompra.login.domain.usecase.SignInAnonymouslyUseCase
import dev.bonygod.listacompra.login.domain.usecase.UpdateNombreUseCase
import dev.bonygod.listacompra.login.domain.usecase.UserLoginUseCase
import dev.bonygod.listacompra.login.domain.usecase.UserRegisterUseCase
import dev.bonygod.listacompra.login.ui.AuthViewModel
import dev.bonygod.listacompra.login.ui.SplashViewModel
import dev.bonygod.listacompra.mislistas.domain.usecase.AddNewListaUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.DeleteListaUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.GetAlexaConfigUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.GetListasUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.RenameListaUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.SetDefaultListaUseCase
import dev.bonygod.listacompra.mislistas.domain.usecase.SetListaAlexaUseCase
import dev.bonygod.listacompra.mislistas.ui.MisListasViewModel
import dev.bonygod.listacompra.notificaciones.data.NotificacionesApi
import dev.bonygod.listacompra.notificaciones.ui.NotificacionesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single<CrashReporter> { CrashlyticsKMP.reporter }
    single { Navigator(get()) }
    single { PendingHomeAction() }
    single { PendingNotificationAction() }
    single { PreferenciasLocales() }
    single { NetworkProvider().provideFirebaseClient() }
    single { NetworkProvider().provideAnalytics() }
    single { NetworkProvider().provideAuth() }
    single { AnalyticsService(get()) }
    single { ListaCompraDataSource(get(), get()) }
    single { UsersDataSource(get(), get()) }
    single { ProductosRepository(get()) }
    single { UserRepository(get(), get()) }
    single { NotificacionesApi(get()) }
    single<String>(named("API_KEY")) { BuildConfig.FIREBASE_API_KEY }
    single<String>(named("CLIENT_ID")) { BuildConfig.CLIENT_ID }

    single(named("appScope")) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}

val viewModelsModule = module {
    viewModelOf(::ListaCompraViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::MisListasViewModel)
    viewModelOf(::SplashViewModel)
    viewModelOf(::AlexaViewModel)
    viewModelOf(::NotificacionesViewModel)
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
    single { ShareListaCompraUseCase(get(), get(), get(named("appScope"))) }
    single { AddSharedListUseCase(get()) }
    single { DeleteNotificationUseCase(get()) }
    single { DeleteAccountUseCase(get()) }
    single { SharedState() }
    single { GetListasUseCase(get()) }
    single { SetDefaultListaUseCase(get()) }
    single { RenameListaUseCase(get()) }
    single { AddNewListaUseCase(get()) }
    single { DeleteListaUseCase(get()) }
    single { GetAlexaConfigUseCase(get()) }
    single { SetListaAlexaUseCase(get()) }
    single { SignInAnonymouslyUseCase(get()) }
    single { GuardarTokenPushUseCase(get()) }
    single { ResolveSessionUseCase(get(), get(), get(), get(), get(named("appScope"))) }
    single { IsAnonymousUserUseCase(get()) }
    single { LinkAccountWithEmailUseCase(get()) }
    single { UpdateNombreUseCase(get()) }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    // Único punto de arranque común a Android e iOS: initKoin() lo invocan
    // ListaCompraApp.onCreate() y MainViewController(), así que configurar
    // AdMobKMP aquí evita tener dos sitios que mantener sincronizados.
    AdMobKMP.configure(
        AdMobConfig(
            androidBannerId = BuildConfig.ADMOB_ANDROID_BANNER,
            androidInterstitialId = BuildConfig.ADMOB_ANDROID_INTERSTITIAL,
            iosBannerId = BuildConfig.ADMOB_IOS_BANNER,
            iosInterstitialId = BuildConfig.ADMOB_IOS_INTERSTITIAL,
            useTestAds = false, // ⚠️ CAMBIAR A true SOLO PARA DESARROLLO (regla igual a la de AdConstants)
            interstitialEnabled = false,
        )
    )

    startKoin {
        config?.invoke(this)
        modules(appModule, viewModelsModule, dataModule)
    }
}