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
                }.sortedBy { producto ->
                    producto.fecha
                }
            emit(productos)
        }
    }

    suspend fun deleteProductos(id: String) {
        firebase.collection("lista-compra").document(id).delete()
    }

    suspend fun deleteAllProductos() {
        try {
            // Obtener todos los documentos de la colección
            val querySnapshot = firebase.collection("lista-compra").get()

            // Eliminar cada documento individualmente
            querySnapshot.documents.forEach { document ->
                firebase.collection("lista-compra").document(document.id).delete()
            }
        } catch (e: Exception) {
            throw Exception("Error al eliminar todos los productos: ${e.message}", e)
        }
    }
}