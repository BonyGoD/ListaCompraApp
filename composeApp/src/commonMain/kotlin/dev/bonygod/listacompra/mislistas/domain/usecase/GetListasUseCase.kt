package dev.bonygod.listacompra.mislistas.domain.usecase
import dev.bonygod.listacompra.login.data.repository.UserRepository
import dev.bonygod.listacompra.mislistas.domain.model.ListaInfo
class GetListasUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<List<ListaInfo>> {
        return userRepository.getListas()
    }
}
