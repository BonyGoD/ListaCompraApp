package dev.bonygod.listacompra.login.data.datasource

import dev.bonygod.listacompra.login.data.model.UserResponse
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp

class UsersDataSource(
    private val auth: FirebaseAuth,
    private val firebase: FirebaseFirestore
) {
    /**
     * Registrar un nuevo usuario con email y contraseña e incluirlo en la tabla usuarios
     */
    suspend fun userRegister(email: String, password: String): UserResponse {
        val result = auth.createUserWithEmailAndPassword(email, password)
        val newLista = createNewListaCompra()
        firebase.collection("usuarios")
            .document(result.user?.uid.orEmpty())
            .set(
                data = mapOf(
                    "nombre" to result.user?.email?.substringBefore("@").orEmpty(),
                    "email" to result.user?.email.orEmpty(),
                    "apiKey" to "",
                    "listas" to listOf(newLista)
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

    suspend fun userGoogleRegister(uid: String, displayName: String, email: String): UserResponse {
        val userUID = auth.currentUser?.uid.orEmpty()
        val userDoc = firebase.collection("usuarios")
            .document(userUID)
            .get()
        val lista = userDoc.get("listas") as List<String>
        if(userUID.isNotEmpty() && lista.isNotEmpty()) {
            return  UserResponse(
                uid = userUID,
                nombre = userDoc.get("nombre") as? String ?: "",
                email = userDoc.get("email") as? String ?: "",
                apiKey = "",
                listas = userDoc.get("listas") as List<String>
            )
        }
        val newLista = createNewListaCompra()
        firebase.collection("usuarios")
            .document(uid)
            .set(
                data = mapOf(
                    "nombre" to displayName,
                    "email" to email,
                    "apiKey" to "",
                    "listas" to listOf(newLista)
                )
            )
        return UserResponse(
            uid = uid,
            nombre = displayName,
            email = email,
            apiKey = "",
            listas = listOf(newLista)
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
    /**
     * Crear nueva lista al registrar un usuario
     */
    suspend fun createNewListaCompra(): String {
        try {
            val docRef = firebase.collection("lista-compra")
                .add(
                    mapOf(
                        "createdAt" to Timestamp.now()
                    )
                )
            return docRef.id
        } catch (e: Exception) {
            val error = e.message
        }
        return "docRef.id"
    }
}