package com.khamrnet.app.data.dao

import androidx.room.*
import com.khamrnet.app.data.entities.ProductUnit

@Dao
interface ProductUnitDao {
    @Query("SELECT * FROM product_units WHERE productId = :productId")
    suspend fun findByProduct(productId: Long): List<ProductUnit>

    @Insert
    suspend fun insert(u: ProductUnit): Long
}
