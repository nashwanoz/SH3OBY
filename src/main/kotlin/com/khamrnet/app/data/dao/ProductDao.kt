package com.khamrnet.app.data.dao

import androidx.room.*
import com.khamrnet.app.data.entities.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    suspend fun getAll(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): Product?

    @Insert
    suspend fun insert(p: Product): Long
}
