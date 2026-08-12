package net.khamer.link.sync

import com.google.firebase.firestore.FirebaseFirestore

// Simple SyncManager skeleton - expand as needed
class SyncManager {
    private val db = FirebaseFirestore.getInstance()

    fun startListeningProducts(onProductChanged: (Map<String, Any>) -> Unit) {
        db.collection("products")
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                snapshots?.documentChanges?.forEach { dc ->
                    val doc = dc.document
                    onProductChanged(doc.data)
                }
            }
    }

    suspend fun pushProduct(productId: String, data: Map<String, Any>) {
        db.collection("products").document(productId).set(data)
    }
}
