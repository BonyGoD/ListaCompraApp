package dev.bonygod.listacompra.login.data.datasource

import dev.bonygod.listacompra.login.data.model.NotificationsReponse
import dev.bonygod.listacompra.login.data.model.UserResponse
import dev.bonygod.listacompra.login.domain.mapper.toDomain
import dev.bonygod.listacompra.login.domain.model.Notifications
import dev.bonygod.listacompra.mislistas.domain.model.ListaInfo
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

class UsersDataSource(
    private val auth: FirebaseAuth,
    private val firebase: FirebaseFirestore
) {
    private suspend fun createUserDocument(
        uid: String,
        nombre: String,
        email: String,
        merge: Boolean = false
    ): String {
        val newLista = createNewListaCompra()
        firebase.collection("usuarios")
            .document(uid)
            .set(
                data = mapOf(
                    "nombre" to nombre,
                    "email" to email,
                    "apiKey" to "",
                    "listas" to listOf(newLista)
                ),
                merge = merge
            )
        return newLista
    }

    /**
     * Registrar un nuevo usuario con email y contraseña e incluirlo en la tabla usuarios
     */
    suspend fun userRegister(email: String, password: String): UserResponse {
        val result = auth.createUserWithEmailAndPassword(email, password)
        val nombre = result.user?.email?.substringBefore("@").orEmpty()
        val userEmail = result.user?.email.orEmpty()
        createUserDocument(result.user?.uid.orEmpty(), nombre, userEmail)
        return UserResponse(
            uid = result.user?.uid.orEmpty(),
            nombre = nombre,
            email = userEmail,
            apiKey = "",
            listas = listOf()
        )
    }

    suspend fun userGoogleRegister(uid: String, displayName: String, email: String): UserResponse {
        val userUID = auth.currentUser?.uid.orEmpty()
        val userDoc = firebase.collection("usuarios")
            .document(userUID)
            .get()
        if (userDoc.exists) {
            return UserResponse(
                uid = userUID,
                nombre = userDoc.get("nombre") as? String ?: "",
                email = userDoc.get("email") as? String ?: "",
                apiKey = "",
                listas = userDoc.get("listas") as List<String>
            )
        }
        val newLista = createUserDocument(uid, displayName, email)
        return UserResponse(
            uid = uid,
            nombre = displayName,
            email = email,
            apiKey = "",
            listas = listOf(newLista)
        )
    }

    suspend fun hasActiveSession(): Boolean {
        val restoredUser = withTimeoutOrNull(3.seconds) {
            auth.authStateChanged.first()
        }
        return (restoredUser ?: auth.currentUser) != null
    }

    fun isAnonymous(): Boolean = auth.currentUser?.isAnonymous == true

    suspend fun signInAnonymously(): UserResponse {
        val result = auth.signInAnonymously()
        val uid = result.user?.uid.orEmpty()
        val newLista = createUserDocument(uid, "", "")
        return UserResponse(
            uid = uid,
            nombre = "",
            email = "",
            apiKey = "",
            listas = listOf(newLista)
        )
    }

    suspend fun repairUserDocument(uid: String, nombre: String, email: String): UserResponse {
        val newLista = createUserDocument(uid, nombre, email, merge = true)
        return UserResponse(
            uid = uid,
            nombre = nombre,
            email = email,
            apiKey = "",
            listas = listOf(newLista)
        )
    }

    private suspend fun updateUserProfile(uid: String, nombre: String, email: String) {
        firebase.collection("usuarios")
            .document(uid)
            .set(
                data = mapOf(
                    "nombre" to nombre,
                    "email" to email
                ),
                merge = true
            )
    }

    suspend fun linkWithEmail(email: String, password: String): UserResponse {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("No hay una sesión activa que vincular.")
        val credential = EmailAuthProvider.credential(email, password)
        val result = currentUser.linkWithCredential(credential)
        val uid = result.user?.uid.orEmpty()
        val nombre = email.substringBefore("@")
        updateUserProfile(uid, nombre, email)
        val userDoc = firebase.collection("usuarios").document(uid).get()
        return UserResponse(
            uid = uid,
            nombre = nombre,
            email = email,
            apiKey = userDoc.get("apiKey") as? String ?: "",
            listas = (userDoc.get("listas") as? List<String>) ?: emptyList()
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
                        "createdAt" to Timestamp.now(),
                        "owner" to auth.currentUser?.uid
                    )
                )
            return docRef.id
        } catch (e: Exception) {
            throw Exception("Error al crear lista: ${e.message}", e)
        }
    }

    /**
     * Obtener notificaciones del usuario
     */
    fun getNotifications(): Flow<Notifications> {
        val userEmail = auth.currentUser?.email.orEmpty()
        if (userEmail.isEmpty()) {
            return flowOf(Notifications(emptyList()))
        }
        return firebase
            .collection("notifications")
            .where { "email".equalTo(userEmail) }
            .snapshots
            .map { querySnapshot ->
                val notificationsList = querySnapshot.documents.map { doc ->
                    doc.data<NotificationsReponse>().toDomain()
                }
                Notifications(notificationsList)
            }
    }

    /**
     * Mandar notificación para compartir lista a un usuario
     */
    suspend fun shareListaCompra(nombre: String, listaId: String, email: String) {
        try {
            // El nombre de la lista se resuelve aquí, donde sí hay permiso: quien
            // comparte lo tiene en su propio documento. El destinatario no puede
            // leer la lista ajena, así que viaja con la invitación.
            val userUID = auth.currentUser?.uid.orEmpty()
            val userDoc = firebase.collection("usuarios").document(userUID).get()
            @Suppress("UNCHECKED_CAST")
            val nombresListas = (userDoc.get("nombresListas") as? Map<String, String>) ?: emptyMap()
            val listaNombre = nombresListas[listaId] ?: try {
                firebase.collection("lista-compra").document(listaId).get()
                    .get("nombre") as? String
            } catch (e: Exception) {
                null
            }

            // Crear la notificación en la colección notifications
            firebase.collection("notifications")
                .add(
                    mapOf(
                        "nombre" to nombre,
                        "email" to email.trim().lowercase(),
                        "listaId" to listaId,
                        "listaNombre" to listaNombre.orEmpty()
                    )
                )
        } catch (e: Exception) {
            throw Exception("Error al compartir la lista: ${e.message}", e)
        }
    }

    suspend fun addSharedList(listaId: String, listaNombre: String): UserResponse {
        val uid = auth.currentUser?.uid.orEmpty()
        val userDoc = firebase.collection("usuarios").document(uid).get()
        val currentListas = (userDoc.get("listas") as? List<String>) ?: emptyList()
        val updatedListas = if (currentListas.contains(listaId)) {
            currentListas
        } else {
            currentListas + listaId
        }
        @Suppress("UNCHECKED_CAST")
        val currentNombres = (userDoc.get("nombresListas") as? Map<String, String>) ?: emptyMap()
        val updatedNombres = if (listaNombre.isBlank()) {
            currentNombres
        } else {
            currentNombres + (listaId to listaNombre)
        }
        firebase.collection("usuarios")
            .document(uid)
            .set(
                data = mapOf(
                    "listas" to updatedListas,
                    "nombresListas" to updatedNombres
                ),
                merge = true
            )
        return UserResponse(
            uid = uid,
            nombre = auth.currentUser?.displayName.orEmpty(),
            email = auth.currentUser?.email.orEmpty(),
            apiKey = "",
            listas = updatedListas
        )
    }

    suspend fun deleteNotification(listaId: String) {
        val userEmail = auth.currentUser?.email.orEmpty()
        firebase
            .collection("notifications")
            .where {
                "email".equalTo(userEmail) and "listaId".equalTo(listaId)
            }
            .get()
            .documents
            .forEach { doc ->
                doc.reference.delete()
            }

    }

    suspend fun deleteOwnerList() {
        val userUID = auth.currentUser?.uid.orEmpty()
        firebase
            .collection("lista-compra")
            .where {
                "owner".equalTo(userUID)
            }
            .get()
            .documents
            .forEach { doc ->
                doc.reference.delete()
            }
    }

    suspend fun deletFirestoreAccount(currentUser: FirebaseUser?) {
        val userUID = currentUser?.uid.orEmpty()
        val userEmail = currentUser?.email.orEmpty()
        try {
            firebase
                .collection("notifications")
                .where {
                    "email".equalTo(userEmail)
                }
                .get()
                .documents
                .forEach { doc ->
                    doc.reference.delete()
                }
            firebase
                .collection("usuarios")
                .document(userUID)
                .delete()
        } catch (e: Exception) {
            throw Exception("Error al borrar la cuenta de Firestore: ${e.message}", e)
        }
    }

    suspend fun deleteAccount() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            throw Exception("No hay usuario logueado. Por favor, inicia sesión para borrar la cuenta.")
        }
        deleteOwnerList()
        deletFirestoreAccount(currentUser)
        try {
            currentUser.delete()
        } catch (e: Exception) {
            if (e.message?.contains("recently authenticated") == true ||
                e.message?.contains("re-authenticate") == true) {
                throw Exception("Por seguridad, debes volver a iniciar sesión antes de borrar la cuenta.")
            } else {
                throw Exception("Error al borrar la cuenta de autenticación: ${e.message}")
            }
        }
        logOut()
    }

    /**
     * Obtener info de todas las listas del usuario.
     * El nombre se lee primero del mapa `nombresListas` en el doc del usuario
     * (que el usuario siempre puede escribir), y hace fallback al doc de lista-compra.
     */
    suspend fun getListas(): List<ListaInfo> {
        val userUID = auth.currentUser?.uid.orEmpty()
        val userDoc = firebase.collection("usuarios").document(userUID).get()
        val listaIds = (userDoc.get("listas") as? List<String>) ?: emptyList()
        val defaultListaId = listaIds.firstOrNull() ?: ""
        @Suppress("UNCHECKED_CAST")
        val nombresListas = (userDoc.get("nombresListas") as? Map<String, String>) ?: emptyMap()

        return listaIds.map { listaId ->
            val nombre = nombresListas[listaId] ?: try {
                val listaDoc = firebase.collection("lista-compra").document(listaId).get()
                listaDoc.get("nombre") as? String ?: "Lista de la compra"
            } catch (e: Exception) {
                "Lista de la compra"
            }
            ListaInfo(id = listaId, nombre = nombre, isDefault = listaId == defaultListaId)
        }
    }

    /**
     * Establecer una lista como predeterminada (mueve al inicio del array en Firestore)
     */
    suspend fun setDefaultLista(listaId: String) {
        val userUID = auth.currentUser?.uid.orEmpty()
        val userDoc = firebase.collection("usuarios").document(userUID).get()
        val currentListas = (userDoc.get("listas") as? List<String>) ?: emptyList()
        if (!currentListas.contains(listaId)) return
        val reordered = listOf(listaId) + currentListas.filter { it != listaId }
        firebase.collection("usuarios").document(userUID).set(
            data = mapOf("listas" to reordered),
            merge = true
        )
    }

    /**
     * Renombrar una lista.
     * Guarda el nombre en `usuarios/{uid}.nombresListas` para evitar problemas
     * de permisos en `lista-compra/{listaId}` (documentos sin campo owner, listas compartidas…)
     */
    suspend fun renameNombreLista(listaId: String, nombre: String) {
        val userUID = auth.currentUser?.uid.orEmpty()
        val userDoc = firebase.collection("usuarios").document(userUID).get()
        @Suppress("UNCHECKED_CAST")
        val currentNombres = (userDoc.get("nombresListas") as? Map<String, String>) ?: emptyMap()
        val updatedNombres = currentNombres + (listaId to nombre)
        firebase.collection("usuarios").document(userUID).set(
            data = mapOf("nombresListas" to updatedNombres),
            merge = true
        )
    }

    /**
     * Crear una nueva lista y añadirla al usuario.
     * También guarda el nombre inicial en `nombresListas`.
     */
    suspend fun addNewLista(nombre: String): ListaInfo {
        val userUID = auth.currentUser?.uid.orEmpty()
        val docRef = firebase.collection("lista-compra").add(
            mapOf(
                "createdAt" to Timestamp.now(),
                "owner" to userUID,
                "nombre" to nombre
            )
        )
        val newListaId = docRef.id
        val userDoc = firebase.collection("usuarios").document(userUID).get()
        val currentListas = (userDoc.get("listas") as? List<String>) ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val currentNombres = (userDoc.get("nombresListas") as? Map<String, String>) ?: emptyMap()
        firebase.collection("usuarios").document(userUID).set(
            data = mapOf(
                "listas" to (currentListas + newListaId),
                "nombresListas" to (currentNombres + (newListaId to nombre))
            ),
            merge = true
        )
        return ListaInfo(id = newListaId, nombre = nombre, isDefault = false)
    }
}
