package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.login.domain.model.Usuario

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
    private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase
) {
    suspend operator fun invoke(): Result<Usuario> {
        if (!userRepo.hasActiveSession()) {
            return signInAnonymouslyUseCase()
        }
        return getUserUseCase().fold(
            onSuccess = { usuario ->
                if (usuario.listas.isEmpty()) {
                    userRepo.repairUserDocument(usuario.uid, usuario.nombre, usuario.email)
                } else {
                    Result.success(usuario)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
}
