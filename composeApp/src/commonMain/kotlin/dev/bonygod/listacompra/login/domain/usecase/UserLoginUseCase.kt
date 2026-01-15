package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.core.CustomFailures.LoginFailure
import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.login.domain.model.Usuario
import dev.bonygod.listacompra.util.isValidEmail

class UserLoginUseCase(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Usuario> {
        if(!email.isValidEmail()) {
            return Result.failure(LoginFailure.IncorrectEmail())
        }
        return userRepo.loginWithEmail(email, password)
    }
}