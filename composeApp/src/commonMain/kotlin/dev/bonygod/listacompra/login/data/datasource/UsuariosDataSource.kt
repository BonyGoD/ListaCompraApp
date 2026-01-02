package dev.bonygod.listacompra.login.data.datasource

import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.firestore.FirebaseFirestore

class UsuariosDataSource(
    private val auth: FirebaseAuth,
    private val firebase: FirebaseFirestore
) {
    /**
     * Registrar un nuevo usuario con email y contraseña e incluirlo en la tabla usuarios
     */
    suspend fun registrarUsuario(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password)
            firebase.collection("usuarios")
                .document(result.user?.uid.orEmpty())
                .set(
                    data = mapOf(
                        "nombre" to result.user?.email?.substringBefore("@").orEmpty(),
                        "email" to result.user?.email.orEmpty(),
                        "apiKey" to "",
                        "listas" to listOf<String>()
                    )
                )
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login con email y contraseña
     */
    suspend fun loginConEmail(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password)
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login con Google
     */
    suspend fun loginConGoogle(idToken: String): Result<FirebaseUser?> {
        return try {
            val credential = GoogleAuthProvider.credential(idToken, null)
            val result = auth.signInWithCredential(credential)
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Recuperar contraseña
     */
    suspend fun recuperarPass(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cerrar sesión
     */
    suspend fun cerrarSesion() {
        auth.signOut()
    }

    /**
     * Obtener el usuario actual
     */
    fun obtenerUsuarioActual(): FirebaseUser? {
        return auth.currentUser
    }
}