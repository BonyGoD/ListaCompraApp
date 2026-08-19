package dev.bonygod.listacompra.login.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository

/**
 * Consulta local, sin red: si la sesión actual es anónima.
 * Delegado fino sobre `UserRepository.isAnonymous()`, que a su vez lee
 * `auth.currentUser?.isAnonymous` de forma síncrona.
 */
class IsAnonymousUserUseCase(
    private val userRepo: UserRepository
) {
    operator fun invoke(): Boolean {
        return userRepo.isAnonymous()
    }
}
