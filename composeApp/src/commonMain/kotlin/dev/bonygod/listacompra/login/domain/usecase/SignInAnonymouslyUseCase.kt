package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.login.domain.model.Usuario

class SignInAnonymouslyUseCase(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(): Result<Usuario> {
        return userRepo.signInAnonymously()
    }
}
