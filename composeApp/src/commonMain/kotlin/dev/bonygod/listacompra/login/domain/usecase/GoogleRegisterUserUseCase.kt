package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.login.domain.model.Usuario

class GoogleRegisterUserUseCase(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(uid: String, displayName:String, email: String): Result<Usuario> {
        return userRepo.googleRegister(uid, displayName, email)

    }
}