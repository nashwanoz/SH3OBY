package com.khamrnet.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_units")
data class ProductUnit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val name: String, // e.g., "حبة", "كرتون"
    val multiplier: Double = 1.0 // factor relative to base unit
)
