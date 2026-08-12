package net.khamer.link.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocks")
data class Stock(
    @PrimaryKey val productId: String,
    val quantityInBase: Long
)
