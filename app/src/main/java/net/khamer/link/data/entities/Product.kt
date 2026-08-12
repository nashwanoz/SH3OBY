package net.khamer.link.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val sku: String?,
    val description: String?,
    val createdAt: Long
)
