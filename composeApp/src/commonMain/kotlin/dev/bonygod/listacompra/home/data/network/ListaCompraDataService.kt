package dev.bonygod.listacompra.home.data.network

import dev.bonygod.listacompra.home.domain.mapper.toDomain
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
                _root_ide_package_.dev.bonygod.listacompra.home.data.datasource.model.entity.ProductoResponse(
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

    suspend fun updateProducto(id: String, nombre: String, isImportant: Boolean) {
        firebase.collection("lista-compra").document(id).set(
            data = mapOf(
                "producto" to nombre,
                "isImportant" to isImportant
            ),
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