package dev.bonygod.listacompra.login.data.repository

import dev.bonygod.crashlytics.kmp.core.CrashReporter
import dev.bonygod.listacompra.core.CustomFailures.LoginFailure
import dev.bonygod.listacompra.core.CustomFailures.toUserFailure
import dev.bonygod.listacompra.login.data.datasource.UsersDataSource
import dev.bonygod.listacompra.login.domain.mapper.toDomain
import dev.bonygod.listacompra.login.domain.model.Notifications
import dev.bonygod.listacompra.login.domain.model.Usuario
import dev.bonygod.listacompra.mislistas.domain.model.ListaInfo
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val usersDS: UsersDataSource,
    private val crashReporter: CrashReporter
) {
    suspend fun userRegister(email: String, password: String): Result<Usuario> {
        return try {
            val userResponse = usersDS.userRegister(email, password)
            Result.success(userResponse.toDomain())
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.userRegister")
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
            crashReporter.recordException(e, "UserRepository.loginWithEmail")
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            usersDS.resetPassword(email)
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.resetPassword")
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
            crashReporter.recordException(e, "UserRepository.getActualUser")
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun hasActiveSession(): Boolean {
        return usersDS.hasActiveSession()
    }

    fun isAnonymous(): Boolean {
        return usersDS.isAnonymous()
    }

    suspend fun signInAnonymously(): Result<Usuario> {
        return try {
            val userResponse = usersDS.signInAnonymously()
            Result.success(userResponse.toDomain())
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.signInAnonymously")
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun repairUserDocument(uid: String, nombre: String, email: String): Result<Usuario> {
        return try {
            val userResponse = usersDS.repairUserDocument(uid, nombre, email)
            Result.success(userResponse.toDomain())
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.repairUserDocument")
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun googleRegister(uid: String, displayName: String, email: String): Result<Usuario> {
        return try {
            val userResponse = usersDS.userGoogleRegister(uid, displayName, email)
            Result.success(userResponse.toDomain())
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.googleRegister")
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun linkWithEmail(email: String, password: String): Result<Usuario> {
        return try {
            val userResponse = usersDS.linkWithEmail(email, password)
            Result.success(userResponse.toDomain())
        } catch (e: FirebaseAuthUserCollisionException) {
            crashReporter.recordException(e, "UserRepository.linkWithEmail")
            Result.failure(LoginFailure.CredentialAlreadyInUse())
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.linkWithEmail")
            Result.failure(e.toUserFailure())
        }
    }

    fun getNotifications(): Flow<Notifications?> {
        return try {
            usersDS.getNotifications()
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.getNotifications")
            throw e.toUserFailure()
        }
    }

    suspend fun shareListaCompra(nombre: String, listaId: String, email: String): Result<Unit> {
        return try {
            usersDS.shareListaCompra(nombre, listaId, email)
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.shareListaCompra")
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun addSharedList(listaId: String, listaNombre: String): Result<Usuario> {
        return try {
            val userResponse = usersDS.addSharedList(listaId, listaNombre)
            Result.success(userResponse.toDomain())
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.addSharedList")
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun deleteNotification(listaId: String): Result<Unit> {
        return try {
            usersDS.deleteNotification(listaId)
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.deleteNotification")
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            usersDS.deleteAccount()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.deleteAccount")
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun getListas(): Result<List<ListaInfo>> {
        return try {
            Result.success(usersDS.getListas())
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.getListas")
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun setDefaultLista(listaId: String): Result<Unit> {
        return try {
            usersDS.setDefaultLista(listaId)
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.setDefaultLista")
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun renameNombreLista(listaId: String, nombre: String): Result<Unit> {
        return try {
            usersDS.renameNombreLista(listaId, nombre)
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.renameNombreLista")
            Result.failure(e.toUserFailure())
        }
    }

    suspend fun addNewLista(nombre: String): Result<ListaInfo> {
        return try {
            Result.success(usersDS.addNewLista(nombre))
        } catch (e: Exception) {
            crashReporter.recordException(e, "UserRepository.addNewLista")
            Result.failure(e.toUserFailure())
        }
    }
}
