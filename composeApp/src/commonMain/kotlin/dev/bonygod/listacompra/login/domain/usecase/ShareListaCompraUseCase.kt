package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.core.CustomFailures.LoginFailure
import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.notificaciones.data.NotificacionesApi
import dev.bonygod.listacompra.util.isValidEmail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ShareListaCompraUseCase(
    private val userRepo: UserRepository,
    private val notificacionesApi: NotificacionesApi,
    private val appScope: CoroutineScope
) {
    suspend operator fun invoke(nombre: String, listaId: String, email: String): Result<Unit> {
        if (!email.isValidEmail()) {
            return Result.failure(LoginFailure.IncorrectEmail())
        }
        val resultado = userRepo.shareListaCompra(nombre, listaId, email)
        if (resultado.isSuccess) {
            appScope.launch {
                try {
                    notificacionesApi.avisarListaCompartida(email, listaId)
                } catch (e: Exception) {
                }
            }
        }
        return resultado
    }
}
