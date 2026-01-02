package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository

class ResetPasswordUseCase(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return userRepo.resetPassword(email)
    }
}