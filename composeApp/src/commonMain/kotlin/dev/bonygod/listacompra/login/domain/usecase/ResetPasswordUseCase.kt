package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.core.CustomFailures.LoginFailure
import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.util.isValidEmail

class ResetPasswordUseCase(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (!email.isValidEmail()) {
            return Result.failure(LoginFailure.IncorrectEmail())
        }
        return userRepo.resetPassword(email)
    }
}