package dev.bonygod.listacompra.core.network

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

class NetworkProvider {

    fun provideFirebaseClient() = Firebase.firestore
}