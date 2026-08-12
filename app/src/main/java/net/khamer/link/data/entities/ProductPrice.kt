package net.khamer.link.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_prices")
data class ProductPrice(
    @PrimaryKey val id: String,
    val productUnitId: String,
    val price: Double,
    val createdAt: Long
)
