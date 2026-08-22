package dev.bonygod.listacompra.mislistas.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.mislistas.domain.model.AlexaConfig

class GetAlexaConfigUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Result<AlexaConfig> = userRepository.getConfigAlexa()
}
