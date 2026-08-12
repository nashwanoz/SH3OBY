package net.khamer.link.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_transactions")
data class StockTransaction(
    @PrimaryKey val id: String,
    val productId: String,
    val deltaInBase: Long,
    val reason: String,
    val refId: String?,
    val date: Long,
    val userId: String?
)
