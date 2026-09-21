package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.login.domain.model.Usuario
import dev.bonygod.listacompra.notificaciones.PushNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Resuelve la sesión con la que arranca la app (usado por el Splash):
 * - Con sesión activa, reutiliza al usuario. Si su documento se quedó sin listas
 *   (documento perdido o escritura fallida en un arranque anterior), lo repara
 *   antes de devolverlo, sin tocar el uid de la sesión ya activa.
 * - Sin sesión, crea una sesión anónima nueva.
 */
class ResolveSessionUseCase(
    private val userRepo: UserRepository,
    private val getUserUseCase: GetUserUseCase,
    private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase,
    private val guardarTokenPushUseCase: GuardarTokenPushUseCase,
    private val appScope: CoroutineScope
) {
    suspend operator fun invoke(): Result<Usuario> {
        if (!userRepo.hasActiveSession()) {
            return signInAnonymouslyUseCase()
        }
        val resultado = getUserUseCase().fold(
            onSuccess = { usuario ->
                if (usuario.listas.isEmpty()) {
                    userRepo.repairUserDocument(usuario.uid, usuario.nombre, usuario.email)
                } else {
                    Result.success(usuario)
                }
            },
            onFailure = { Result.failure(it) }
        )
        if (resultado.isSuccess && !userRepo.isAnonymous()) {
            appScope.launch { refrescarTokenPush() }
        }
        return resultado
    }

    private suspend fun refrescarTokenPush() {
        if (!PushNotifications.hasPermission()) return
        val token = PushNotifications.getToken() ?: return
        guardarTokenPushUseCase(token)
    }
}
