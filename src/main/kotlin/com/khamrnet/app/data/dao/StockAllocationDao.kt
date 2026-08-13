package com.khamrnet.app.data.dao

import androidx.room.*
import com.khamrnet.app.data.entities.StockAllocation

@Dao
interface StockAllocationDao {
    @Insert
    suspend fun insert(a: StockAllocation): Long

    @Query("SELECT * FROM stock_allocations WHERE warehouseId = :warehouseId AND productId = :productId LIMIT 1")
    suspend fun find(warehouseId: Long, productId: Long): StockAllocation?

    @Query("UPDATE stock_allocations SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Double)

    @Query("SELECT * FROM stock_allocations WHERE posted = 0")
    suspend fun findUnposted(): List<StockAllocation>

    @Query("UPDATE stock_allocations SET posted = 1 WHERE id = :id")
    suspend fun markPosted(id: Long)
}
