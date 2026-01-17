package dev.bonygod.listacompra.login.data.datasource

import dev.bonygod.listacompra.login.data.model.UserResponse
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.firestore.FirebaseFirestore

class UsersDataSource(
    private val auth: FirebaseAuth,
    private val firebase: FirebaseFirestore
) {
    /**
     * Registrar un nuevo usuario con email y contraseña e incluirlo en la tabla usuarios
     */
    suspend fun userRegister(email: String, password: String): UserResponse {
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
        return UserResponse(
            uid = result.user?.uid.orEmpty(),
            nombre = result.user?.email?.substringBefore("@").orEmpty(),
            email = result.user?.email.orEmpty(),
            apiKey = "",
            listas = listOf()
        )
    }

    /**
     * Login con email y contraseña
     * Autentica y obtiene los datos del usuario desde Firestore
     */
    suspend fun loginWithEmail(email: String, password: String): UserResponse {
        val result = auth.signInWithEmailAndPassword(email, password)
        val uid = result.user?.uid.orEmpty()

        val userDoc = firebase.collection("usuarios")
            .document(uid)
            .get()

        return UserResponse(
            uid = uid,
            nombre = userDoc.get("nombre") as? String ?: "",
            email = result.user?.email.orEmpty(),
            apiKey = userDoc.get("apiKey") as? String ?: "",
            listas = (userDoc.get("listas") as? List<String>) ?: emptyList()
        )
    }

    /**
     * Recuperar contraseña
     */
    suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
    }

    /**
     * Cerrar sesión
     */
    suspend fun logOut() {
        auth.signOut()
    }

    /**
     * Obtener el usuario actual
     */
    suspend fun getActualUser(): UserResponse {
        val userUID = auth.currentUser?.uid.orEmpty()
        val userDoc = firebase.collection("usuarios")
            .document(userUID)
            .get()

        return UserResponse(
            uid = userUID,
            nombre = userDoc.get("nombre") as? String ?: "",
            email = userDoc.get("email") as? String ?: "",
            apiKey = userDoc.get("apiKey") as? String ?: "",
            listas = (userDoc.get("listas") as? List<String>) ?: emptyList()
        )
    }
}