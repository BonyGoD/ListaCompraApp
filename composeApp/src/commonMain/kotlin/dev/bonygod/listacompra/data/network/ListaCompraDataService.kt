package dev.bonygod.listacompra.data.network

import dev.bonygod.listacompra.data.datasource.model.entity.ProductoResponse
import dev.bonygod.listacompra.domain.mapper.toDomain
import dev.bonygod.listacompra.util.timeStampTransform
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.flow.flow

class ListaCompraDataService(
    private val firebase: dev.gitlive.firebase.firestore.FirebaseFirestore
) {
    fun getProductos() = flow {
        firebase.collection("lista-compra").snapshots.collect { querySnapshot ->
            val productos = querySnapshot.documents.map { documentSnapshot ->
                val fechaTimestamp = documentSnapshot.get("fecha") as? Timestamp
                ProductoResponse(
                    id = documentSnapshot.id,
                    producto = documentSnapshot.get("producto") as? String ?: "",
                    fecha = fechaTimestamp?.timeStampTransform() ?: ""
                ).toDomain()
            }
            emit(productos)
        }
    }
}