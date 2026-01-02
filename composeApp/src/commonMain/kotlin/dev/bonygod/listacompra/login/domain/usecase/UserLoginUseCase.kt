package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.login.domain.model.Usuario

class UserLoginUseCase(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Usuario> {
        return userRepo.loginWithEmail(email, password)
    }
}