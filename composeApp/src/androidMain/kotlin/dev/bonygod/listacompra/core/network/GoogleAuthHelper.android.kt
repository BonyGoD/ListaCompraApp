package dev.bonygod.listacompra.core.network

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

actual class GoogleAuthHelper(
    private val context: Context,
    private val credentialManager: CredentialManager
) : KoinComponent {
    actual suspend fun signInWithGoogle(
        onSuccess: (String, String, String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        val clientId: String by inject(named("CLIENT_ID"))
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val fireBase = FirebaseAuth.getInstance()
                val user = fireBase.signInWithCredential(authCredential).await().user
                val uid = user?.uid
                val profileImageUrl = user?.photoUrl?.toString()

                onSuccess(user?.displayName ?: "user", uid ?: "", user?.email ?: "", profileImageUrl ?: "")
            } else {
                onError("Invalid credential type")
            }
        } catch (e: NoCredentialException) {
            onError("No email found in your device")
        } catch (e: GetCredentialException) {
            onError("GetCredentialException: ${e.message}")
        } catch (e: Exception) {
            onError("Error: ${e.message}")
        }
    }
}