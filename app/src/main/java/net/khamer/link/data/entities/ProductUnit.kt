package net.khamer.link.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_units")
data class ProductUnit(
    @PrimaryKey val id: String,
    val productId: String,
    val name: String,
    val multiplierToBase: Long
)
