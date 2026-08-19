package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.core.CustomFailures.LoginFailure
import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.login.domain.model.Usuario
import dev.bonygod.listacompra.util.isValidEmail

/**
 * Vincula la sesión anónima activa a una cuenta de email/contraseña real,
 * conservando el `uid` (y con él, `listas`/`nombresListas`) de la sesión
 * anónima. Delegado fino sobre `UserRepository.linkWithEmail`.
 */
class LinkAccountWithEmailUseCase(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Usuario> {
        if (!email.isValidEmail()) {
            return Result.failure(LoginFailure.IncorrectEmail())
        }
        if (password.isEmpty()) {
            return Result.failure(LoginFailure.BlankPassword())
        }
        return userRepo.linkWithEmail(email, password)
    }
}
