package dev.bonygod.listacompra.core.network

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import org.koin.core.component.KoinComponent

class NetworkProvider {

    fun provideFirebaseClient() = Firebase.firestore
}