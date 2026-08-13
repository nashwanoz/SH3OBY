package com.khamrnet.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_allocations")
data class StockAllocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val warehouseId: Long,
    val quantity: Double,
    val posted: Int = 0
)
