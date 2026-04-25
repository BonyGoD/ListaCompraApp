package dev.bonygod.listacompra.mislistas.domain.usecase

import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.mislistas.domain.model.ListaInfo

class AddNewListaUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(nombre: String): Result<ListaInfo> =
        userRepository.addNewLista(nombre)
}

