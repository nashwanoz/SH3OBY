package net.khamer.link.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.khamer.link.data.entities.Product
import net.khamer.link.data.entities.ProductUnit

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(p: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(u: ProductUnit)

    @Query("SELECT * FROM product_units WHERE productId = :productId")
    suspend fun unitsForProduct(productId: String): List<ProductUnit>
}
