package dev.bonygod.listacompra.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bonygod.listacompra.core.navigation.Navigator
import dev.bonygod.listacompra.core.navigation.Routes
import dev.bonygod.listacompra.login.domain.usecase.ResolveSessionUseCase
import dev.bonygod.listacompra.login.ui.composables.interactions.SplashEvent
import dev.bonygod.listacompra.login.ui.composables.interactions.SplashState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val navigator: Navigator,
    private val resolveSessionUseCase: ResolveSessionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state

    init {
        resolveSession()
    }

    fun onEvent(event: SplashEvent) {
        when (event) {
            is SplashEvent.Retry -> resolveSession()
            is SplashEvent.GoToLogin -> navigator.clearAndNavigateTo(Routes.Login)
        }
    }

    private fun resolveSession() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                resolveSessionUseCase().fold(
                    onSuccess = { usuario ->
                        navigator.clearAndNavigateTo(Routes.Home(usuario.uid))
                    },
                    onFailure = { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}
