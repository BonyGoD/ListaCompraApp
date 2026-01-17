package dev.bonygod.listacompra.login.data.repository

import dev.bonygod.listacompra.core.CustomFailures.toUserFailure
import dev.bonygod.listacompra.login.data.datasource.UsersDataSource
import dev.bonygod.listacompra.login.domain.mapper.toDomain
import dev.bonygod.listacompra.login.domain.model.Usuario

class UserRepository(
    private val usersDS: UsersDataSource
) {
    suspend fun userRegister(email: String, password: String): Result<Usuario> {
        return try {
            val userResponse = usersDS.userRegister(email, password)
            Result.success(userResponse.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toUserFailure())
        }
    }

    /**
     * Login con email y contraseña
     * Obtiene el UserResponse del DataSource y lo mapea a Usuario
     */
    suspend fun loginWithEmail(email: String, password: String): Result<Usuario> {
        return try {
            val userResponse = usersDS.loginWithEmail(email, password)
            Result.success(userResponse.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            usersDS.resetPassword(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun logOut() {
        usersDS.logOut()
    }

    suspend fun getActualUser(): Result<Usuario> {
        return try {
            val userResponse = usersDS.getActualUser()
            Result.success(userResponse.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toUserFailure())
        }
    }
}