package dev.bonygod.listacompra.data.network

import dev.bonygod.listacompra.data.datasource.model.entity.ProductoResponse
import dev.bonygod.listacompra.domain.mapper.toDomain
import dev.bonygod.listacompra.util.timeStampTransform
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    suspend fun updateProducto(id: String, nombre: String) {
        firebase.collection("lista-compra").document(id).set(
            data = mapOf("producto" to nombre),
            merge = true
        )
    }

    suspend fun deleteProductos(id: String) {
        firebase.collection("lista-compra").document(id).delete()
    }

    suspend fun deleteAllProductos() {
        try {
            val querySnapshot = firebase.collection("lista-compra").get()

            coroutineScope {
                querySnapshot.documents.map { document ->
                    async { firebase.collection("lista-compra").document(document.id).delete() }
                }.awaitAll()
            }
        } catch (e: Exception) {
            throw Exception("Error al eliminar todos los productos: ${e.message}", e)
        }
    }

    suspend fun addProducto(producto: String) {
        try {
            firebase.collection("lista-compra").add(
                data = mapOf(
                    "producto" to producto,
                    "fecha" to Timestamp.now()
                )
            )
        } catch (e: Exception) {
            throw Exception("Error al insertar el producto: ${e.message}", e)
        }
    }
}