package com.khamrnet.app.data.dao

import androidx.room.*
import com.khamrnet.app.data.entities.Warehouse

@Dao
interface WarehouseDao {
    @Insert
    suspend fun insert(w: Warehouse): Long

    @Query("SELECT * FROM warehouses WHERE ownerUserId = :userId LIMIT 1")
    suspend fun findByOwner(userId: Long): Warehouse?

    @Query("SELECT * FROM warehouses WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): Warehouse?

    @Query("SELECT * FROM warehouses")
    suspend fun getAll(): List<Warehouse>
}
