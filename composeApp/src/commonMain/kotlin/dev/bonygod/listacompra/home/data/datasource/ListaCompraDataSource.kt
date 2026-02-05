package dev.bonygod.listacompra.home.data.datasource

import dev.bonygod.listacompra.home.data.model.entity.ProductoResponse
import dev.bonygod.listacompra.home.domain.mapper.toDomain
import dev.bonygod.listacompra.util.timeStampTransform
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flow

class ListaCompraDataSource(
    private val firebase: FirebaseFirestore
) {
    fun getProductos(listaId: String) = flow {
        val productosCollection =
            firebase.collection("lista-compra").document(listaId).collection("productos")
        productosCollection.snapshots.collect { querySnapshot ->
            val productos = querySnapshot.documents.map { documentSnapshot ->
                val fechaTimestamp = documentSnapshot.get("fecha") as? Timestamp
                ProductoResponse(
                    id = documentSnapshot.id,
                    producto = documentSnapshot.get("producto") as? String ?: "",
                    fecha = fechaTimestamp?.timeStampTransform() ?: "",
                    isImportant = documentSnapshot.get("isImportant") as? Boolean ?: false
                ).toDomain()
            }.sortedBy { producto ->
                producto.fecha
            }
            emit(productos)
        }
    }

    suspend fun updateProducto(listaId: String, id: String, nombre: String, isImportant: Boolean) {
        val productosCollection =
            firebase.collection("lista-compra").document(listaId).collection("productos")
        productosCollection.document(id).set(
            data = mapOf(
                "producto" to nombre,
                "isImportant" to isImportant
            ),
            merge = true
        )
    }

    suspend fun deleteProductos(listaId: String, id: String) {
        val productosCollection =
            firebase.collection("lista-compra").document(listaId).collection("productos")
        productosCollection.document(id).delete()
    }

    suspend fun deleteAllProductos(listaId: String) {
        try {
            val productosCollection =
                firebase.collection("lista-compra").document(listaId).collection("productos")
            val querySnapshot = productosCollection.get()

            coroutineScope {
                querySnapshot.documents.map { document ->
                    async { productosCollection.document(document.id).delete() }
                }.awaitAll()
            }
        } catch (e: Exception) {
            throw Exception("Error al eliminar todos los productos: ${e.message}", e)
        }
    }

    suspend fun addProducto(listaId: String, producto: String) {
        try {
            if (listaId.isNotEmpty()) {
                val productosCollection =
                    firebase.collection("lista-compra").document(listaId).collection("productos")
                productosCollection.add(
                    data = mapOf(
                        "producto" to producto,
                        "fecha" to Timestamp.now(),
                        "isImportant" to false
                    )
                )
            } else {
                val docRef = firebase.collection("lista-compra")
                    .add(
                        mapOf(
                            "createdAt" to Timestamp.now()
                        )
                    )
                firebase.collection("lista-compra").document(docRef.id)
                    .collection("productos")
                    .add(
                        mapOf(
                            "producto" to producto,
                            "fecha" to Timestamp.now(),
                            "isImportant" to false
                        )
                    )
            }
        } catch (e: Exception) {
            throw Exception("Error al insertar el producto: ${e.message}", e)
        }
    }
}